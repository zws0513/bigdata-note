package com.zw.pool

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies, OffsetRange}

/**
  * Created by zhangws on 2019/2/1.
  */
object KafkaManager {

  /**
    * 创建kafkaStream
    *
    * @param ssc         StreamingContext
    * @param kafkaParams kafka参数
    * @param topics      topics
    * @return kafkaStream
    */
  def createDirectKafkaStream[K, V](ssc: StreamingContext,
                                    kafkaParams: Map[String, Object],
                                    topics: Set[String]): InputDStream[ConsumerRecord[K, V]] = {
    KafkaUtils.createDirectStream[K, V](ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[K, V](topics, kafkaParams))
  }

//  /**
//    * 创建kafkaStream
//    * <p>
//    * 根据ZK中保存的offset创建
//    *
//    * @param ssc         StreamingContext
//    * @param kafkaParams kafka参数
//    * @param zkHosts     zkHosts
//    * @param groupId     groupId
//    * @param topics      topics
//    * @return kafkaStream
//    */
//  def createDirectKafkaStreamFromZk[K, V](ssc: StreamingContext,
//                                          kafkaParams: Map[String, Object],
//                                          zkHosts: String,
//                                          groupId: String,
//                                          zkNode: String,
//                                          topics: Set[String]): InputDStream[ConsumerRecord[K, V]] = {
//    val storedOffsets = readZkOffsets(zkHosts, groupId, topics.last)
//    val kafkaStream = storedOffsets match {
//      case None => // start from the latest offsets
//        KafkaUtils.createDirectStream[K, V](ssc,
//          LocationStrategies.PreferConsistent,
//          ConsumerStrategies.Subscribe[K, V](topics, kafkaParams))
//      case Some(fromOffsets) => // start from previously saved offsets
//        KafkaUtils.createDirectStream[K, V](ssc,
//          LocationStrategies.PreferConsistent,
//          ConsumerStrategies.Subscribe[K, V](topics,
//            kafkaParams,
//            fromOffsets))
//    }
//    kafkaStream
//  }

  /**
    * 创建kafkaStream
    * <p>
    * 根据Kafka中保存的offset创建
    *
    * @param ssc         StreamingContext
    * @param kafkaParams kafka参数
    * @param topics      topics
    * @return kafkaStream
    */
  def createDirectKafkaStreamFromKafka[K, V](ssc: StreamingContext,
                                             kafkaParams: Map[String, Object],
                                             topics: Set[String]): InputDStream[ConsumerRecord[K, V]] = {
    KafkaUtils.createDirectStream[K, V](ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[K, V](topics, kafkaParams))
  }

//  /**
//    * Read the previously saved offsets from Zookeeper
//    *
//    * @param zkHosts zkHosts
//    * @param groupId groupId
//    * @param topic   topic
//    * @return offset
//    */
//  private def readZkOffsets(zkHosts: String,
//                            groupId: String,
//                            topic: String): Option[Map[TopicPartition, Long]] = {
//    val topicDirs = new ZKGroupTopicDirs(groupId, topic)
//    val zkClient = new ZkClient(zkHosts)
//    val children = zkClient.countChildren(s"${topicDirs.consumerOffsetDir}")
//
//    var fromOffsets: Map[TopicPartition, Long] = Map.empty
//    if (children > 0) {
//
//      for (i <- 0 until children) {
//        val partitionOffset = zkClient.readData[String](s"${topicDirs.consumerOffsetDir}/$i")
//        val tp = new TopicPartition(topic, i)
//        fromOffsets += (tp -> partitionOffset.toLong) //将不同 partition 对应的 offset 增加到 fromOffsets 中
//      }
//    }
//    zkClient.close()
//
//    if (fromOffsets.nonEmpty) {
//      Option(fromOffsets)
//    } else {
//      None
//    }
//  }

//  /**
//    * 保存offset到zk
//    *
//    * @param zkHosts       zkHosts
//    * @param offsetsRanges offset
//    * @param zkPath        zkPath
//    */
//  def saveZkOffsets(zkHosts: String,
//                    offsetsRanges: Array[OffsetRange],
//                    zkPath: String = ""): Unit = {
//    val offsetsRangesStr = offsetsRanges.map(offsetRange => s"${offsetRange.partition}:${offsetRange.fromOffset}")
//      .mkString(",")
//    val zkClient = new ZkClient(zkHosts)
//    ZkUtils(zkClient, isZkSecurityEnabled = false).updatePersistentPath(zkPath, offsetsRangesStr)
//    zkClient.close()
//  }

  def main(args: Array[String]): Unit = {
    //    val fromOffset = readZkOffsets("10.10.121.57:2181", "")
  }
}
