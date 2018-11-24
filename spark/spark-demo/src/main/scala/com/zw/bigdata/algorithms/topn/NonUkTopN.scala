package com.zw.bigdata.algorithms.topn

import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.SortedMap
import scala.collection.immutable.TreeMap

/**
  * 获取TopN数据(非唯一主键)
  * <p>
  * 数据格式:
  * A,2
  * B,2
  * <p>
  * 参数:
  * 输入目录
  * TopN: 前或后几名
  * direction: 方向("top", "bottom")
  * 输出目录
  * <p>
  * Created by zhangws on 17/3/2.
  */
object NonUkTopN {

    def main(args: Array[String]) {
        if (args.length < 3) {
            println("Usage: TopN <file> N direction")
            System.exit(1)
        }
        val N = args(1).toInt
        val direction = args(2)

        val conf = new SparkConf().setAppName(NonUkTopN.getClass.getSimpleName)
            .setMaster("local")
        val sc = new SparkContext(conf)
        val broadcastN = sc.broadcast(N)
        val broadcastDirection = sc.broadcast(direction)

        val filesPath = System.getProperty("user.dir") + args(0) + "non_uk_topn*"
        val lines = sc.textFile(filesPath)
//        lines.foreach(println)

        val partitions = lines.coalesce(9)
            .map(l => {
                val tokens = l.split(",")
                (tokens(0), tokens(1).toInt)
            })
            .reduceByKey(_+_)
            .mapPartitions[SortedMap[Int, String]](t => {
                var localTopN: SortedMap[Int, String] = new TreeMap()
                while (t.hasNext) {
                    val attr = t.next()
                    localTopN = localTopN + ((attr._2, attr._1))
                    if (localTopN.size > broadcastN.value) {
                        if ("top".equals(broadcastDirection.value)) {
                            localTopN = localTopN - localTopN.firstKey
                        } else {
                            localTopN = localTopN - localTopN.lastKey
                        }
                    }
                }
                Iterator(localTopN)
            })

        //////// 使用collect
        val allTop10 = partitions.collect()
        var finalTop10 = new TreeMap[Int, String]()
        allTop10.foreach(v => {
            v.foreach(m => {
                finalTop10 = finalTop10 + m
                if (finalTop10.size > broadcastN.value) {
                    if ("top".equals(broadcastDirection.value)) {
                        finalTop10 = finalTop10 - finalTop10.firstKey
                    } else {
                        finalTop10 = finalTop10 - finalTop10.lastKey
                    }
                }
            })
        })

        finalTop10.foreach(println)
    }
}
