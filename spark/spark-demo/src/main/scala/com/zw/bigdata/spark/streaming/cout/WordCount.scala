package com.zw.bigdata.spark.streaming.cout

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Created by zhangws on 16/11/12.
  */
object WordCount {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName(WordCount.getClass.getSimpleName)
      .setMaster("local[*]")
    val ssc = new StreamingContext(conf, Seconds(1))

    val lines = ssc.socketTextStream("www.baidu.com", 80)

    val words = lines.flatMap(_.split(" "))
    val pairs = words.map(word => (word, 1))
    val wordCounts = pairs.reduceByKey(_ + _)

    wordCounts.print()

    ssc.start()
    ssc.awaitTerminationOrTimeout(60000)
  }

}
