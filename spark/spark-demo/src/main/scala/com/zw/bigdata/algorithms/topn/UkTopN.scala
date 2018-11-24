package com.zw.bigdata.algorithms.topn

import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.SortedMap
import scala.collection.immutable.TreeMap

/**
  * 获取TopN数据(唯一主键)
  * <p>
  * 数据格式:
  * cat_weight Double
  * cat_id String
  * cat_name String
  * <p>
  * 参数:
  * 输入文件
  * TopN: 前或后几名
  * direction: 方向("top", "bottom")
  * 输出目录
  * <p>
  * Created by zhangws on 17/3/2.
  */
object UkTopN {

    def main(args: Array[String]) {
        if (args.length < 3) {
            println("Usage: TopN <file> N direction")
            System.exit(1)
        }
        val N = args(1).toInt
        val direction = args(2)

        val conf = new SparkConf().setAppName(UkTopN.getClass.getSimpleName)
            .setMaster("local[*]")
        val sc = new SparkContext(conf)
        val broadcastN = sc.broadcast(N)
        val broadcastDirection = sc.broadcast(direction)

        val filePath = System.getProperty("user.dir") + args(0)
        val partitions = sc.textFile(filePath, 1)
            .map(v => {
                val s = v.split(",")
                (s(1), s(0).toInt)
            })
            .mapPartitions[SortedMap[Int, String]](v => {
            var top10: SortedMap[Int, String] = new TreeMap[Int, String]()
            while (v.hasNext) {
                val attr = v.next()
                top10 = top10 + ((attr._2, attr._1))
                if (top10.size > broadcastN.value) {
                    if ("top".equals(broadcastDirection.value)) {
                        top10 = top10 - top10.firstKey
                    } else {
                        top10 = top10 - top10.lastKey
                    }
                }
            }
            Iterator(top10)
        })

        //////// 使用collect
        //        val allTop10 = partitions.collect()
        //        var finalTop10 = new TreeMap[Int, String]()
        //        allTop10.foreach(v => {
        //            v.foreach(m => {
        //                finalTop10 = finalTop10 + m
        //                if (finalTop10.size > broadcastN.value) {
        //                    if ("top".equals(broadcastDirection.value)) {
        //                        finalTop10 = finalTop10 - finalTop10.firstKey
        //                    } else {
        //                        finalTop10 = finalTop10 - finalTop10.lastKey
        //                    }
        //                }
        //            })
        //        })
        //
        //        finalTop10.foreach(println)

        //////// 使用reduce
        partitions.reduce((m1, m2) => {
            var top10: SortedMap[Int, String] = new TreeMap[Int, String]()
            m1.foreach(v1 => {
                top10 = top10 + v1
                if (top10.size > broadcastN.value) {
                    if ("top".equals(broadcastDirection.value)) {
                        top10 = top10 - top10.firstKey
                    } else {
                        top10 = top10 - top10.lastKey
                    }
                }
            })
            m2.foreach(v2 => {
                top10 = top10 + v2
                if (top10.size > broadcastN.value) {
                    if ("top".equals(broadcastDirection.value)) {
                        top10 = top10 - top10.firstKey
                    } else {
                        top10 = top10 - top10.lastKey
                    }
                }
            })
            top10
        })
            .foreach(println)
    }
}
