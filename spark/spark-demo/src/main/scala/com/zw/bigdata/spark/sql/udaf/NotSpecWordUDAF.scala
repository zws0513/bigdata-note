package com.zw.bigdata.spark.sql.udaf

import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.expressions.{MutableAggregationBuffer, UserDefinedAggregateFunction}
import org.apache.spark.sql.types._

/**
  * 用于聚合时
  * <p>
  * 维度聚合时，用于名称列，获取其中非'unknown'的值
  *
  *   spark.sqlContext.udf.register("notSpecWord", new NotSpecWord)
  *
  * select id, notSpecWord(name), count(id) from table group by id;
  * </p>
  * Created by zhangws on 2018/2/1.
  */
class NotSpecWordUDAF extends UserDefinedAggregateFunction {

  private val UNKNOWN = "unknown"

  override def inputSchema: StructType = {
    StructType(Array(StructField("str", StringType, nullable = true)))
  }

  override def bufferSchema: StructType = {
    StructType(Array(StructField("word", StringType, nullable = true)))
  }

  override def dataType: DataType = {
    StringType
  }

  override def deterministic: Boolean = {
    true
  }

  override def initialize(buffer: MutableAggregationBuffer): Unit = {
    buffer(0) = UNKNOWN
  }

  override def update(buffer: MutableAggregationBuffer, input: Row): Unit = {
    try {
      buffer(0) = if (input.getAs[String](0) != UNKNOWN
        && !input.getAs[String](0).contains("xC")) input.getAs[String](0)
    } catch {
      case e: Exception => println(e.getMessage)
    }
  }

  override def merge(buffer1: MutableAggregationBuffer, buffer2: Row): Unit = {
    try {
      buffer1(0) = if (buffer1.getAs[String](0) != UNKNOWN
        && buffer1.getAs[String](0).contains("xC")) buffer1.getAs[String](0)
      else buffer2.getAs[String](0)
    } catch {
      case e: Exception => println(e.getMessage)
    }
  }

  override def evaluate(buffer: Row): Any = {
    if (buffer.getAs[String](0) == UNKNOWN) "" else buffer.getAs[String](0)
  }
}

object NotSpecWordUDAF {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("NotSpecWordUDAF")
      .master("local")
      .getOrCreate()

    // 构造模拟数据
    val names = Seq(
      Row(1, "unknown", 1),
      Row(1, "unknown", 2),
      Row(2, "unknown", 3),
      Row(2, "test", 1),
      Row(2, null, 1))
    val namesRDD = spark.sparkContext.parallelize(names)
    val structType = StructType(List(
      StructField("id", IntegerType, nullable = true),
      StructField("name", StringType, nullable = true),
      StructField("num", IntegerType, nullable = true)))
    val namesDF = spark.createDataFrame(namesRDD, structType)

    // 注册一张names表
    namesDF.createOrReplaceTempView("names")

    // 定义和注册自定义函数
    // 定义函数：自己写匿名函数
    // 注册函数：SQLContext.udf.register()
    spark.sqlContext.udf.register("notSpecWord", new NotSpecWordUDAF())

    // 使用自定义函数
    spark.sqlContext.sql("select id, notSpecWord(name) from names group by id")
      .collect()
      .foreach(println)
  }
}
