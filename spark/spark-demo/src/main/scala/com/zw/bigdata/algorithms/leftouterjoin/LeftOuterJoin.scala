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
object LeftOuterJoin {

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
                (user(0), ("L", user(1)))
            })
        val transactionsRDD = sc.textFile(workDir + args(1))
            .map(v => {
                val transaction = v.split(" ")
                (transaction(2), ("P", transaction(1)))
            })

        transactionsRDD.union(usersRDD)

            /**
              * groupByKey输出
              *
              * (u5,CompactBuffer((P,p4), (L,GA)))
              * (u1,CompactBuffer((P,p3), (P,p1), (P,p1), (P,p4), (L,UT)))
              * (u2,CompactBuffer((P,p1), (P,p2), (L,GA)))
              * (u3,CompactBuffer((L,CA)))
              * (u4,CompactBuffer((P,p4), (L,CA)))
              */
            .groupByKey()

            /**
              * flatMap输出结果
              *
              * (p4,GA)
              * (p3,UT)
              * (p1,UT)
              * (p1,UT)
              * (p4,UT)
              * (p1,GA)
              * (p2,GA)
              * (p4,CA)
              */
            .flatMap(v => {
                val pairs = v._2
                var location = "UNKNOWN"
                var products = List[String]()
                pairs.foreach(p => {
                    if (p._1 == "L") {
                        location = p._2
                    } else {
                        products = products ::: List(p._2)
                    }
                })
                var kvList = List.empty[(String, String)]
                products.foreach(e => {
                    kvList = kvList ::: List((e, location))
                })
                kvList
            })

            /**
              * groupByKey输出结果
              *
              * (p4,CompactBuffer(GA, UT, CA))
              * (p1,CompactBuffer(UT, UT, GA))
              * (p2,CompactBuffer(GA))
              * (p3,CompactBuffer(UT))
              */
            .groupByKey

            /**
              * mapValues输出结果
              *
              * (p4,(Set(UT, CA, GA),3))
              * (p1,(Set(UT, GA),2))
              * (p2,(Set(GA),1))
              * (p3,(Set(UT),1))
              */
            .mapValues(v => {
                // 去掉重复项
                var uniqueLocations = new scala.collection.mutable.HashSet[String]
                v.foreach(e => {
                    uniqueLocations += e
                })
                (uniqueLocations, uniqueLocations.size)
            })
            .collect
            .foreach(println)
    }
}
