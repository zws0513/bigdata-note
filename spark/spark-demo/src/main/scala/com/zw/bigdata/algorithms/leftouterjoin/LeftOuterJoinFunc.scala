package com.zw.bigdata.algorithms.leftouterjoin

import org.apache.spark.{SparkConf, SparkContext}

/**
  * 数据:
  * users: user_id location_id
  * transaction: transaction_id product_id user_id quantity amount
  *
  * 运行参数:
  * /data/chapter04/users
  * /data/chapter04/transactions
  *
  * Created by zhangws on 17/3/2.
  */
object LeftOuterJoinFunc {

  def main(args: Array[String]) {
    if (args.length < 2) {
      System.err.println("Usage: LeftOuterJoin <users> <transactions>")
      System.exit(1)
    }
    val conf = new SparkConf().setAppName(LeftOuterJoin.getClass.getSimpleName)
      .setMaster("local[*]")
    val sc = new SparkContext(conf)

    val workDir = System.getProperty("user.dir")

    val usersRDD = sc.textFile(workDir + args(0))
      .map(v => {
        val user = v.split(" ")
        (user(0), user(1))
      })
    val transactionsRDD = sc.textFile(workDir + args(1))
      .map(v => {
        val transaction = v.split(" ")
        (transaction(2), transaction(1))
      })

    transactionsRDD.leftOuterJoin(usersRDD)

      /**
        * map后的输出
        *
        * (p4,Some(GA))
        * (p3,Some(UT))
        * (p1,Some(UT))
        * (p1,Some(UT))
        * (p4,Some(UT))
        * (p4,Some(CA))
        * (p1,Some(GA))
        * (p2,Some(GA))
        */
      .map(v => {
      (v._2._1, v._2._2)
    })
      //            .groupByKey
      //            .mapValues(v => {
      //                var uniqueLocations = new scala.collection.mutable.HashSet[String]
      //                v.foreach(e => {
      //                    uniqueLocations += e.get
      //                })
      //                (uniqueLocations, uniqueLocations.size)
      //            })

      /**
        * combineByKey输出结果
        *
        * (p2,Set(GA))
        * (p4,Set(GA, UT, CA))
        * (p1,Set(UT, GA))
        * (p3,Set(UT))
        */
      .combineByKey(v => {
      Set(v.get)
    },
      (s: Set[String], v: Option[String]) => {
        s + v.get
      },
      (s1: Set[String], s2: Set[String]) => {
        s1 ++ s2
      }
    )
      .map(v => {
        (v._1, (v._2, v._2.size))
      })
      .collect
      .foreach(println)
  }
}
