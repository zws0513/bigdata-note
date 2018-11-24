package com.zw.bigdata.algorithms.secondarysort

import org.apache.spark.{SparkConf, SparkContext}

import scala.util.Sorting

/**
  * 二次排序
  *
  * 输入格式:
  * 年,月,日,温度
  *
  * 输出:
  * 年-月 当月内的温度(升序)
  *
  * 运行参数:
  * /data/chapter01/secondarysort
  *
  * Created by zhangws on 17/3/2.
  */
object SecondarySort {

    def main(args: Array[String]) {
        if (args.length < 1) {
            println("Usage: SecondarySort <file>")
            System.exit(1)
        }
        val filePath = System.getProperty("user.dir") + args(0)
        val conf = new SparkConf().setAppName(SecondarySort.getClass.getSimpleName)
            .setMaster("local[*]")
        val sc = new SparkContext(conf)
        sc.textFile(filePath, 1)
            // 格式: 年,月,日,温度
            //   2012,01,01,5
            // 转为:
            //  年-月,温度
            //  2012-01, 5
            .map(v => {
                val values = v.split(",")
                (values(0) + "-" + values(1), values(3).toInt)
            })
            // 将每个Key对应的Value合并到一个集合Iterable[V]中
            // (2001-01, (11,3,15))
            .groupByKey()

            /**
              * Key不变, 与新的value一起组成新的RDD
              *
              * 2001-01	3,11,15
              * 2002-01	13,34
              * 2012-01	3,5,15,45,51
              */
            .mapValues(v => {
                val arr = v.toArray
                Sorting.quickSort(arr)
                arr
            })
            .collect()
            .foreach(v => {
                println(v._1 + "\t" + v._2.mkString(","))
            })
    }
}
