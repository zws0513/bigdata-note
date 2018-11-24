package com.zw.bigdata.algorithms.topn

import com.zw.bigdata.algorithms.topn.NonUkTopN
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
object UkTopNUsingTakeOrdered {

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

        lines.coalesce(9)
            .map(l => {
                val tokens = l.split(",")
                (tokens(0), tokens(1).toInt)
            })
            .reduceByKey(_+_)
            .takeOrdered(broadcastN.value)(new Ordering[(String, Int)]() {
                override def compare(x: (String, Int), y: (String, Int)): Int = {
                    if ("top".equals(broadcastDirection.value)) {
                        y._2.compareTo(x._2)
                    } else {
                        x._2.compareTo(y._2)
                    }
                }
            })
            .foreach(println)
    }
}
