package com.zw.bigdata.algorithms.market

import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ArrayBuffer

/**
  * 购物车商品对被同时购买的频率
  *
  * 输入:
  *  每一行是一条购物车购买记录
  * 输出:
  *  (商品A,商品B), 频率
  * Created by zhangws on 17/3/27.
  */
object MarketBasketAnalysis {

    def main(args: Array[String]) {
        if (args.length < 1) {
            System.err.println("Usage: <input folder>")
            System.exit(1)
        }

        //        test()


        val workDir = System.getProperty("user.dir")

        val conf = new SparkConf().setAppName(MarketBasketAnalysis.getClass.getSimpleName)
            .setMaster("local")
        val sc = new SparkContext(conf)

        val transactionsRDD = sc.textFile(workDir + args(0), 1)
            .flatMap(t => {
                val list = t.split(",").toList
                val combinations = findSortedCombinations(list)
                var result = Seq[(Seq[String], Int)]()
                combinations.foreach(c => {
                    if (c.nonEmpty) {
                        result ++= Seq((c, 1))
                    }
                })
                result
            })
            .reduceByKey(_ + _)
            .flatMap(v => {
                var result = Seq[(Seq[String], (Seq[String], Int))]()
                val list = v._1
                val frequency = v._2
                result ++= Seq((list, (null, frequency)))
                if (list.size > 1) {
                    list.foreach(l => {
                        val sublist = list.diff(Seq(l))
                        result ++= Seq((sublist, (list, frequency)))
                    })
                }
                result
            })
            .groupByKey()
            .map(v => {
                var result = Seq[(Seq[String], Seq[String], Double)]()
                val fromList = v._1
                val to = v._2

                var toList = Seq[(Seq[String], Int)]()
                var fromCount: (Seq[String], Int) = null
                to.foreach(t2 => {
                    if (t2._1 == null) {
                        fromCount = t2
                    } else {
                        toList ++= Seq(t2)
                    }
                })
                if (toList.nonEmpty) {
                    toList.foreach(t2 => {
                        val confidence: Double = t2._2 / fromCount._2
                        var t2List = t2._1
                        t2List = t2List.diff(fromList)
                        result ++= Seq((fromList, t2List, confidence))
                    })
                }
                result
            })
            .collect()
            .foreach(println)
    }

    def test() {
        val list = List("a", "b", "c", "d")
        System.out.println(list)
        val comb = findSortedCombinations(list)
        System.out.println(comb.size)
        System.out.println(comb)
    }

    def findSortedCombinations[T <: Comparable[T]](elements: Traversable[T]): Seq[Seq[T]] = {
        var result: Seq[Seq[T]] = ArrayBuffer[ArrayBuffer[T]]()
        for (i <- 0 to elements.size) {
            result ++= findSortedCombinations(elements, i)
        }
        result
    }

    def findSortedCombinations[T <: Comparable[T]](elements: Traversable[T],
                                                n: Int): Seq[Seq[T]] = {
        var result: ArrayBuffer[ArrayBuffer[T]] = ArrayBuffer[ArrayBuffer[T]]()
        if (n == 0) {
            result += new ArrayBuffer[T]()
        } else {
            val combinations = findSortedCombinations(elements, n - 1)
            combinations.foreach(c => {
                elements.foreach(e => {
                    if (!c.contains(e)) {
                        var list = ArrayBuffer[T]()
                        list ++= c
                        if (!list.contains(e)) {
                            list ++= ArrayBuffer(e)
                            list = list.sortWith(_.compareTo(_) < 0)
                            if (!result.contains(list)) {
                                result += list
                            }
                        }
                    }
                })
            })
        }
        result
    }
}
