package com.zw.bigdata.spark.streaming.cout

import com.alibaba.fastjson.JSON
import kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Created by zhangws on 16/11/13.
  */
object UserClickCountAnalytics {

  val KAFKA_SERVER: String = "hsm01:9092,hss01:9092,hss02:9092"
  //    val KAFKA_ADDR: String = KAFKA_SERVER + ":9092"
  //    val KAFKA_USER_TOPIC: String = "user_events"
  val KAFKA_USER_TOPIC: String = "p4xlogstash"
  val KAFKA_RECO_TOPIC: String = "reco6"

  def main(args: Array[String]): Unit = {
    var masterUrl = "local[1]"
    if (args.length > 0) {
      masterUrl = args(0)
    }

    val conf = new SparkConf().setAppName(UserClickCountAnalytics.getClass.getSimpleName)
    val ssc = new StreamingContext(conf, Seconds(5))

    val topics = KAFKA_USER_TOPIC.split("\\,").toSet
    println(s"Topics: ${topics}.")

    val brokers = KAFKA_SERVER
    val kafkaParams = Map[String, String](
      "metadata.broker.list" -> brokers,
      "serializer.class" -> "kafka.serializer.StringEncoder"
    )

    val clickHashKey = "app::users::click"

    val kafkaStream = KafkaUtils
      .createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)
    val event = kafkaStream.flatMap(line => {
      println(line)
      val data = JSON.parseObject(line._2)
      Some(data)
    })

    val userClicks = event.map(
      x => (x.getString("uid"), x.getLong("click_count")))
      .reduceByKey(_ + _)

    userClicks.foreachRDD(rdd => {
      rdd.foreachPartition(partitionOfRecords => {
        val jedis = RedisClient.pool.getResource
        partitionOfRecords.foreach(pair => {
          try {
            val uid = pair._1
            val clickCount = pair._2
            jedis.hincrBy(clickHashKey, uid, clickCount)
            println(s"Update uid ${uid} to ${clickCount}.")
          } catch {
            case e: Exception => println("error:" + e)
          }
        })
      })
    })

    ssc.start()
    ssc.awaitTermination()
  }

}
