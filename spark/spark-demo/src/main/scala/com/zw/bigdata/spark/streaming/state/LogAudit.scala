package com.zw.bigdata.spark.streaming.state

import com.esotericsoftware.kryo.Kryo
import com.zw.pool.KafkaManager
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.serializer.KryoRegistrator
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka010.{CanCommitOffsets, HasOffsetRanges, OffsetRange}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable

/**
  * Created by zhangws on 2019/2/1.
  */
object LogAudit {

  protected lazy val logger: Logger = LoggerFactory.getLogger(getClass.getName)

  def main(args: Array[String]): Unit = {

    if (args.length < 9) {
      System.err.println(
        """
          Usage: brokers groupId sourceTopic batchDuration stateTimeOut cpDir jdbc user password offsetReset
        """)
      System.exit(1)
    }

    val Array(
    brokers,
    groupId,
    sourceTopic,
    batchDuration,
    stateTimeOut,
    cpDir,
    jdbc,
    user,
    password) = args.take(9)

    val offsetReset = if (args.length > 9) args(9) else "earliest"

    val kafkaParams = Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> brokers,
      ConsumerConfig.GROUP_ID_CONFIG -> groupId,
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> offsetReset,
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> (false: java.lang.Boolean)
    )


    val ssc = setupSsc(
      sourceTopic.split(",").toSet,
      kafkaParams,
      batchDuration.toInt,
      stateTimeOut.toInt,
      cpDir,
      jdbc,
      user,
      password
    )

    ssc.start()
    ssc.awaitTermination()
  }

  /**
    * 创建SparkStreamingContext, 并处理业务逻辑
    *
    * @param topics        Kafka输入的Topic列表
    * @param kafkaParams   Kafka参数列表
    * @param batchDuration 批大小
    * @param timeout       state保存超时时间
    * @param cpDir         CheckPoint目录
    * @return 创建SparkStreamingContext实例
    */
  def setupSsc(
                topics: Set[String],
                kafkaParams: Map[String, Object],
                batchDuration: Int,
                timeout: Int,
                cpDir: String,
                jdbc: String,
                user: String,
                password: String
              )(): StreamingContext = {
    val conf = new SparkConf().setAppName("AccessTableAudit")
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    conf.set("spark.kryo.registrator", classOf[CustomKryoRegistrator].getName)
            conf.set("spark.streaming.kafka.maxRatePerPartition", "10000")
            conf.setMaster("local[*]")
    val ssc = new StreamingContext(conf, Seconds(batchDuration))

    //    ssc.sparkContext.setLogLevel(Constants.LOG_LEVEL)

    val stateSpec = StateSpec.function(update _)
      .timeout(Seconds(timeout))

    var offsetRanges = Array[OffsetRange]()
    val stream = KafkaManager.createDirectKafkaStream[String, String](ssc, kafkaParams, topics)

    stream.transform(rdd => {
      offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
      rdd
    })
      .map(decode)
      .filter(v => v._1 != ManagerConst.GARBAGE && v._1 != ManagerConst.NOT_SUPPORT)
      .mapWithState(stateSpec)
      .foreachRDD { rdd =>
        if (!rdd.isEmpty()) {
          rdd.foreachPartition(partition => {
            try {
              partition.foreach(data => {
//                println(data)
                AuditDataSave.save(jdbc, user, password, data)
              })
            } catch {
              case e: Exception => logger.error(e.toString)
            }
          })
        }
        stream.asInstanceOf[CanCommitOffsets].commitAsync(offsetRanges)
      }

    ssc.checkpoint(cpDir)
    ssc
  }

  /**
    * 用户行为记录mapWithState处理函数
    *
    * @param batchTime 当前batch时间
    * @param key       待更新的键(MAC)
    * @param value     当前batch时间的信息(用户行为信息)
    * @param state     待更新的值(用户行为信息)
    * @return
    */
  def update(batchTime: Time,
             key: String,
             value: Option[Map[String, String]],
             state: State[Map[String, String]]
            ): Option[Seq[Map[String, String]]] = {

    def timeOut(key: String, record: Map[String, String]): Map[String, String] = {
      //      var map = new mutable.HashMap[String, String]()
      //      record.get("")
      // 暂时不作处理
      record
    }

    def updateUserSession(key: String, newEvent: Map[String, String]): Map[String, String] = {
      val result = new mutable.HashMap[String, String]()

      if (key.startsWith(ManagerConst.HIVE)) { // hive
        result ++= HiveAudit.state(state, newEvent)

      } else if (key.startsWith(ManagerConst.IMPALA)) { // impala
        result ++= ImpalaAudit.state(state, newEvent)

      } else {
        // nothing to do
      }
      result.toMap
    }

    var arr = new mutable.ArrayBuffer[Map[String, String]]()

    value match {
      case Some(newEvent) =>
        val map = updateUserSession(key, newEvent)
        if (map.nonEmpty) {
          arr += map
        }
      case _ if state.isTimingOut() => // 缓存记录超时
        state.getOption() match {
          case Some(cache) =>
            arr += timeOut(key, cache)
          case None => // nothing to do
        }
    }
    if (arr.nonEmpty) {
      Some(arr)
    } else {
      None
    }
  }

  /**
    * 解码用户行为信息
    *
    * @param record kafka消息实例
    * @return (类型, 用户行为信息)
    */
  def decode(record: ConsumerRecord[String, String]): (String, Map[String, String]) = {
    try {
      parser(record)
    } catch {
      case e: Exception =>
        println(e.getMessage)
        (ManagerConst.GARBAGE, null)
    }
  }

  /**
    * 解析
    *
    * @param record kafka消息实例
    * @return (类型, 用户行为信息)
    */
  def parser(record: ConsumerRecord[String, String]): (String, Map[String, String]) = {
    if (record.topic().startsWith("impala")) {
      ImpalaAudit.parser(record.value())
    } else if (record.topic().startsWith("hive")) {
      HiveAudit.parser(record.value())
    } else {
      (ManagerConst.NOT_SUPPORT, null)
    }
  }
}

class CustomKryoRegistrator extends KryoRegistrator {

  override def registerClasses(kryo: Kryo) {
    kryo.register(classOf[GenericRecord])
    ()
  }

}
