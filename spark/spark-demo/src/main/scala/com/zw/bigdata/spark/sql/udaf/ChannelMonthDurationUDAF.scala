package com.zw.bigdata.spark.sql.udaf

import org.apache.spark.sql.expressions.{MutableAggregationBuffer, UserDefinedAggregateFunction}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{Row, SparkSession}

import scala.collection.mutable

/**
  * 用于聚合时，对ChannelDayDurationUDAF的结果，进行二次处理
  * 以固定顺序拼接（主要是为了把月和日数据一次性计算处理）
  * <p>
  *
  *   spark.sqlContext.udf.register("channelMonthDurationUDAF", new ChannelMonthDurationUDAF)
  *
  * select mac, channelMonthDurationUDAF(dura_map) from table group by day;
  * </p>
  * Created by zhangws on 2018/2/1.
  */
class ChannelMonthDurationUDAF extends UserDefinedAggregateFunction {

  private val SEP = "||"

  override def inputSchema: StructType = {
    StructType(Array(StructField("map", MapType(IntegerType, LongType), nullable = false)))
  }

  override def bufferSchema: StructType = {
    StructType(Array(StructField("map", MapType(IntegerType, LongType), nullable = false)))
  }

  override def dataType: DataType = {
    StringType
  }

  override def deterministic: Boolean = {
    true
  }

  override def initialize(buffer: MutableAggregationBuffer): Unit = {
    buffer.update(0, Map[Int, Long]())
  }

  private def addMapFreq(map1: Map[Int, Long], map2: Map[Int, Long]) = {
    map1 ++ map2.map { case (chn, dura) =>
      (chn, map1.getOrElse(chn, 0L) + dura)
    }
  }

  override def update(buffer: MutableAggregationBuffer, input: Row): Unit = {
    try {
      val map = buffer.getAs[Map[Int, Long]](0)
      val newMap = input.getAs[Map[Int, Long]](0)

      buffer.update(0, addMapFreq(map, newMap))
    } catch {
      case e: Exception => println(e.getMessage)
    }
  }

  override def merge(buffer1: MutableAggregationBuffer, buffer2: Row): Unit = {
    try {
      val map1 = buffer1.getAs[Map[Int, Long]](0)
      val map2 = buffer2.getAs[Map[Int, Long]](0)

      buffer1.update(0, addMapFreq(map1, map2))
    } catch {
      case e: Exception => println(e.getMessage)
    }
  }

  override def evaluate(buffer: Row): Any = {
    val chn = mutable.LinkedHashMap[Int, Long](
      1000001 -> 0, // 电影
      1000002 -> 0, // 电视剧
      1000030 -> 0, // 动漫
      1000004 -> 0, // 综艺
      1000005 -> 0, // 音乐
      1000006 -> 0, // 体育
      1000007 -> 0, // 教育
      1000003 -> 0, // 儿童
      1000014 -> 0, // 游戏
      1000009 -> 0, // 纪录片
      1000010 -> 0 // 娱乐
    )

    for (m <- buffer.getAs[Map[Int, Long]](0) if chn.keySet.contains(m._1)) chn.put(m._1, (m._2 / 60.0).ceil.toLong)
    chn.values.mkString(SEP)
  }
}

object ChannelMonthDurationUDAF {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("ChannelMonthDurationUDAF")
      .master("local")
      .getOrCreate()

    // 构造模拟数据
    val names = Seq(
      Row(1, 1, 1000001, 100),
      Row(1, 1, 1000001, 200),
      Row(1, 2, 1000001, 300),
      Row(1, 2, 1000001, 400),
      Row(1, 1, 1000, 200),
      Row(1, 1, 1001, 300),
      Row(2, 1, 1000010, 100),
      Row(2, 2, 1000010, 700),
      Row(1, 1, 1000, 100))
    val namesRDD = spark.sparkContext.parallelize(names)
    val structType = StructType(List(
      StructField("mac", IntegerType, nullable = true),
      StructField("no", IntegerType, nullable = true),
      StructField("name", IntegerType, nullable = true),
      StructField("num", IntegerType, nullable = true)))
    val namesDF = spark.createDataFrame(namesRDD, structType)

    // 注册一张names表
    namesDF.createOrReplaceTempView("names")

    // 定义和注册自定义函数
    // 定义函数：自己写匿名函数
    // 注册函数：SQLContext.udf.register()
    spark.sqlContext.udf.register("channelDayDurationUDAF", new ChannelDayDurationUDAF[Int](IntegerType))
    spark.sqlContext.udf.register("channelMonthDurationUDAF", new ChannelMonthDurationUDAF())

    // 使用自定义函数
    spark.sqlContext.sql(
      """
        |select
        |  mac,
        |  no,
        |  channelDayDurationUDAF(name, num) as num_map
        |from
        |  names
        |group by
        |  mac,
        |  no
      """.stripMargin)
    //      .collect()
    //      .foreach(println)

    // 使用自定义函数
    spark.sqlContext.sql(
      """
        |select
        |  mac,
        |  channelMonthDurationUDAF(num_map)
        |from (
        |  select
        |    mac,
        |    no,
        |    channelDayDurationUDAF(name, num) as num_map
        |  from
        |    names
        |  group by
        |    mac,
        |    no
        |) t
        |group by
        |  mac
      """.stripMargin)
      .collect()
      .foreach(println)
  }
}