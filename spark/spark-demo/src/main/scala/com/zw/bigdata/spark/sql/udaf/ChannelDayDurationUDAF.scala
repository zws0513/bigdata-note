package com.zw.bigdata.spark.sql.udaf

import org.apache.spark.sql.expressions.{MutableAggregationBuffer, UserDefinedAggregateFunction}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{Row, SparkSession}

/**
  * 用于聚合时，n个维度，已其中n-1个维度分组，未参与分组的维度和度量作为UDAF的参数，
  * 以Map形式输出累计结果（key为参数中的维度，value为度量累计）
  * <p>
  * 使用例
  *
  *   spark.sqlContext.udf.register("channelDayDurationUDAF", new ChannelDayDurationUDAF)
  *
  * select day, channelDayDurationUDAF(id, dura) from table group by day;
  * </p>
  * Created by zhangws on 2018/2/1.
  */
class ChannelDayDurationUDAF[T](valType: DataType) extends UserDefinedAggregateFunction {

  /**
    * 输入数据类型（如果多个，以Array定义）
    *
    * @return
    */
  override def inputSchema: StructType = {
    StructType(Array(StructField("chn", valType, nullable = false),
      StructField("dura", LongType, nullable = false)))
  }

  /**
    * 中间缓存数据的类型
    *
    * @return
    */
  override def bufferSchema: StructType = {
    StructType(Array(StructField("map", MapType(valType, LongType), nullable = false)))
  }

  /**
    * 最终UDAF返回的数据类型
    *
    * @return
    */
  override def dataType: DataType = {
    MapType(valType, LongType)
  }

  /**
    * 幂等性（如果输入相同，返回总是相同，则返回true）
    *
    * @return
    */
  override def deterministic: Boolean = {
    true
  }

  /**
    * 初始化聚合缓存区（由于是分布式算法，要求整个聚合过程中，缓存区类型一致）
    *
    * @param buffer 缓存区根实例
    */
  override def initialize(buffer: MutableAggregationBuffer): Unit = {
    buffer.update(0, Map[T, Long]())
  }

  private def addMapFreq(map1: Map[T, Long], map2: Map[T, Long]) = {
    map1 ++ map2.map { case (chn, dura) =>
      (chn, map1.getOrElse(chn, 0L) + dura)
    }
  }

  /**
    * 更新缓存区
    *
    * @param buffer 缓存区根实例
    * @param input  新数据
    */
  override def update(buffer: MutableAggregationBuffer, input: Row): Unit = {
    try {
      val map = buffer.getAs[Map[T, Long]](0)
      val chn = input.getAs[T](0)
      val dura = input.getAs[Long](1)

      buffer.update(0, addMapFreq(map, Map[T, Long](chn -> dura)))
    } catch {
      case e: Exception => println(e.getMessage)
    }
  }

  /**
    * 合并所有缓存区（需要更新到buffer1中）
    *
    * @param buffer1 聚合缓存区1
    * @param buffer2 聚合缓存区2
    */
  override def merge(buffer1: MutableAggregationBuffer, buffer2: Row): Unit = {
    try {
      val map1 = buffer1.getAs[Map[T, Long]](0)
      val map2 = buffer2.getAs[Map[T, Long]](0)

      buffer1.update(0, addMapFreq(map1, map2))
    } catch {
      case e: Exception => println(e.getMessage)
    }
  }

  /**
    * UDAF最终返回值
    *
    * @param buffer 聚合缓存区
    * @return 返回最终结果
    */
  override def evaluate(buffer: Row): Any = {
    buffer.getAs[Map[T, Long]](0)
  }
}

object ChannelDayDurationUDAF {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("ChannelDayDurationUDAF")
      .master("local")
      .getOrCreate()

    // 用于聚合时，以id分组，累计播放时长，并保留指定id后，以Map形式输出累计结果

    // 构造模拟数据
    val names = Seq(
      Row(1, 1000001, 1),
      Row(1, 1000001, 2),
      Row(1, 1000001, 3),
      Row(1, 1000001, 4),
      Row(1, 1000, 2),
      Row(1, 1001, 3),
      Row(2, 1000010, 1),
      Row(2, 1000010, 7),
      Row(1, 1000, 1))
    val namesRDD = spark.sparkContext.parallelize(names)
    val structType = StructType(List(
      StructField("id", IntegerType, nullable = true),
      StructField("name", IntegerType, nullable = true),
      StructField("num", IntegerType, nullable = true)))
    val namesDF = spark.createDataFrame(namesRDD, structType)

    // 注册一张names表
    namesDF.createOrReplaceTempView("names")

    // 定义和注册自定义函数
    // 定义函数：自己写匿名函数
    // 注册函数：SQLContext.udf.register()
    spark.sqlContext.udf.register("channelDayDurationUDAF", new ChannelDayDurationUDAF[Int](IntegerType))

    // 使用自定义函数
    spark.sqlContext.sql("select id, channelDayDurationUDAF(name, num) from names group by id")
      .collect()
      .foreach(println)
  }
}
