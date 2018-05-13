package com.zw.bigdata.spark.sql.udf

import org.apache.spark.sql.types._
import org.apache.spark.sql.{Row, SparkSession}

/**
  * 演示Spark SQL UDF如何使用
  */
object MaxUDF {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("MaxUDF")
      .master("local")
      .getOrCreate()

    // 构造模拟数据
    val names = Seq(
      Row(1, "unknown", 1, 3, 4, 2),
      Row(1, "unknown", 2, 6, 4, 1)
    )
    val namesRDD = spark.sparkContext.parallelize(names)
    val structType = StructType(List(
      StructField("id", IntegerType, nullable = true),
      StructField("name", StringType, nullable = true),
      StructField("num1", IntegerType, nullable = true),
      StructField("num2", IntegerType, nullable = true),
      StructField("num3", IntegerType, nullable = true),
      StructField("num4", IntegerType, nullable = true)
    ))
    val namesDF = spark.createDataFrame(namesRDD, structType)

    // 注册一张names表
    namesDF.createOrReplaceTempView("names")

    // 定义和注册自定义函数
    // 定义函数：自己写匿名函数
    // 注册函数：SQLContext.udf.register()
    spark.sqlContext.udf.register("maxUDF", (v1: Int, v2: Int, v3: Int, v4: Int) => {
      v1.max(v2).max(v3).max(v4)
    })

    // 使用自定义函数
    spark.sqlContext.sql("select id, name, maxUDF(num1, num2, num3, num4) from names")
      .collect()
      .foreach(println)
  }
}
