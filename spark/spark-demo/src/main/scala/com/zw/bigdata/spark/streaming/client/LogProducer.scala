package com.zw.bigdata.spark.streaming.client

import java.util.Properties
import java.util.concurrent.{Executors, ExecutorService}

import com.alibaba.fastjson.JSONObject
import kafka.javaapi.producer.Producer
import kafka.producer.{KeyedMessage, ProducerConfig}

import scala.util.Random

/**
  * Created by zhangws on 16/11/12.
  */
object LogProducer {

  val KAFKA_SERVER: String = "hsm01:9092,hss01:9092,hss02:9092"
  //    val KAFKA_ADDR: String = KAFKA_SERVER + ":9092"
  //    val KAFKA_USER_TOPIC: String = "user_events"
  val KAFKA_USER_TOPIC: String = "p4xlogstash"
  val KAFKA_RECO_TOPIC: String = "reco6"

  private val users = Array(
    "4A4D769EB9679C054DE81B973ED5D768", "8dfeb5aaafc027d89349ac9a20b3930f",
    "011BBF43B89BFBF266C865DF0397AA71", "f2a8474bf7bd94f0aabbd4cdd2c06dcf",
    "068b746ed4620d25e26055a9f804385f", "97edfc08311c70143401745a03a50706",
    "d7f141563005d1b5d0d3dd30138f3f62", "c8ee90aade1671a21336c721512b817a",
    "6b67c8c700427dee7552f81f3228c927", "a95f22eabc4fd4b580c011a3161a9d9d")

  private val osType = Array("android", "iOS")

  private val random = new Random()

  def getUserID: String = {
    users(random.nextInt(users.length))
  }

  def click: Double = {
    random.nextInt(10)
  }

  def getOSType: String = {
    osType(random.nextInt(2))
  }

  def main(args: Array[String]): Unit = {

    val threadPools: ExecutorService = Executors.newFixedThreadPool(5)

    val topic = KAFKA_USER_TOPIC
    val props: Properties = new Properties()
    props.put("metadata.broker.list", KAFKA_SERVER)
    props.put("serializer.class", "kafka.serializer.StringEncoder")

    val config: ProducerConfig = new ProducerConfig(props)
    val producer = new Producer[String, String](config)

    //        while(true) {
    //            // prepare event data
    //            val event = new JSONObject()
    //            event.put("uid", getUserID)
    //            event.put("event_time", System.currentTimeMillis.toString)
    //            event.put("os_type", "Android")
    //            event.put("click_count", click)
    //
    //            // produce event message
    //            producer.send(new KeyedMessage[String, String](topic, event.toString))
    //            println("Message sent: " + event)
    //
    //            Thread.sleep(200)
    //        }

    try {
      for (i <- 1 to 5) {
        threadPools.execute(new LogProducer(i.toString, topic, producer))
      }
    } finally {
      threadPools.shutdown()
    }
  }
}

class LogProducer(threadName: String, topic: String, producer: Producer[String, String]) extends Runnable {

  override def run(): Unit = {
    while (true) {
      val event = new JSONObject()
      event.put("uid", LogProducer.getUserID)
      event.put("event_time", System.currentTimeMillis.toString)
      event.put("os_type", LogProducer.getOSType)
      event.put("click_count", LogProducer.click)
      producer.send(new KeyedMessage[String, String](topic, event.toString))
      println("Message sent: " + event)

      Thread.sleep(1000)
    }
  }
}
