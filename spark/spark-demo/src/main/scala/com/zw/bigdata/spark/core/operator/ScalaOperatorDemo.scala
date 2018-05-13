package com.zw.bigdata.spark.core.operator

import com.google.gson.Gson
import org.apache.spark.{Partitioner, SparkConf, SparkContext}

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

/**
  * <p>
  * transformation类型的算子
  * flatMap  map  mapPartitions
  * mapPartitionsWithIndex
  * reduceByKey groupByKey
  * aggregateByKey  countByKey  sortByKey
  * filter  join  cogroup  intersection
  * union  distinct  sample
  * repartitions  coalesce  cartesian
  * <p>
  * action类型的算子
  * foreach  reduce  takeSample
  * collect  take  saveAsObjectFile
  * saveAsSequenceFile count
  * <p>
  * Created by zhangws on 16/10/30.
  */
object ScalaOperatorDemo {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName(ScalaOperatorDemo.getClass.getSimpleName).setMaster("local")
    val sc = new SparkContext(conf)

    //        map()
    //        filter
    //        flatMap()
    //        mapPartitions()
    //        mapPartitionsWithIndex
    //        sample()
    //        union(sc)
    //        intersection(sc)
    //        distinct(sc)
    //        groupBy(sc)
    //        aggregateByKey(sc)
    //        sortByKey(sc)
    //        join(sc)
    //        cogroup(sc)
    //        pipe(sc)
    //        coalesce(sc)
    //        cartesian(sc)
    //        repartitionAndSortWithinPartitions(sc)

    // Action算子
    reduce(sc)
  }

  class User(var id: Int, var name: String, var pwd: String, var sex: Int)

  /**
    * map算子
    * <p>
    * map和foreach算子:
    * 1. 循环map调用元的每一个元素;
    * 2. 执行call函数, 并返回.
    * </p>
    */
  private def map() {
    val conf = new SparkConf().setAppName(ScalaOperatorDemo.getClass.getSimpleName).setMaster("local")
    val sc = new SparkContext(conf)

    val datas: Array[String] = Array(
      "{'id':1,'name':'xl1','pwd':'xl123','sex':2}",
      "{'id':2,'name':'xl2','pwd':'xl123','sex':1}",
      "{'id':3,'name':'xl3','pwd':'xl123','sex':2}")

    sc.parallelize(datas)
      .map(v => {
        new Gson().fromJson(v, classOf[User])
      })
      .foreach(user => {
        println("id: " + user.id
          + " name: " + user.name
          + " pwd: " + user.pwd
          + " sex:" + user.sex)
      })
  }

  def filter(): Unit = {
    val conf = new SparkConf().setAppName(ScalaOperatorDemo.getClass.getSimpleName).setMaster("local")
    val sc = new SparkContext(conf)

    val datas = Array(1, 2, 3, 7, 4, 5, 8)

    sc.parallelize(datas)
      .filter(v => v >= 3)
      .foreach(println)
  }

  def flatMap(): Unit = {
    val conf = new SparkConf().setAppName(ScalaOperatorDemo.getClass.getSimpleName).setMaster("local")
    val sc = new SparkContext(conf)

    val datas = Array(
      "aa,bb,cc",
      "cxf,spring,struts2",
      "java,C++,javaScript")

    sc.parallelize(datas)
      .flatMap(line => line.split(","))
      .foreach(println)
  }

  def mapPartitions(): Unit = {
    val conf = new SparkConf().setAppName(ScalaOperatorDemo.getClass.getSimpleName).setMaster("local")
    val sc = new SparkContext(conf)

    val datas = Array("张三1", "李四1", "王五1", "张三2", "李四2",
      "王五2", "张三3", "李四3", "王五3", "张三4")

    sc.parallelize(datas, 3)
      .mapPartitions(
        n => {
          val result = ArrayBuffer[String]()
          while (n.hasNext) {
            result.append(n.next())
          }
          result.iterator
        }
      )
      .foreach(println)
  }

  def sample(): Unit = {
    val conf = new SparkConf().setAppName(ScalaOperatorDemo.getClass.getSimpleName).setMaster("local")

    val sc = new SparkContext(conf)

    val datas = Array(1, 2, 3, 7, 4, 5, 8)

    sc.parallelize(datas)
      .sample(withReplacement = false, 0.5, System.currentTimeMillis)
      .foreach(println)
  }

  /**
    * mapPartitionsWithIndex算子
    */
  def mapPartitionsWithIndex(): Unit = {
    val conf = new SparkConf().setAppName(ScalaOperatorDemo.getClass.getSimpleName).setMaster("local")
    val sc = new SparkContext(conf)

    val datas = Array("张三1", "李四1", "王五1", "张三2", "李四2",
      "王五2", "张三3", "李四3", "王五3", "张三4")

    sc.parallelize(datas, 3)
      .mapPartitionsWithIndex(
        (m, n) => {
          val result = ArrayBuffer[String]()
          while (n.hasNext) {
            result.append("分区索引:" + m + "\t" + n.next())
          }
          result.iterator
        }
      )
      .foreach(println)
  }

  def union(sc: SparkContext): Unit = {
    val datas1 = Array("张三", "李四")
    val datas2 = Array("tom", "gim")

    //        sc.parallelize(datas1)
    //            .union(sc.parallelize(datas2))
    //            .foreach(println)

    (sc.parallelize(datas1) ++ sc.parallelize(datas2))
      .foreach(println)
  }

  def intersection(sc: SparkContext): Unit = {
    val datas1 = Array("张三", "李四", "tom")
    val datas2 = Array("tom", "gim")

    sc.parallelize(datas1)
      .intersection(sc.parallelize(datas2))
      .foreach(println)
  }

  def distinct(sc: SparkContext): Unit = {
    val datas = Array("张三", "李四", "tom", "张三")

    sc.parallelize(datas)
      .distinct()
      .foreach(println)
  }

  def groupBy(sc: SparkContext): Unit = {
    sc.parallelize(1 to 9, 3)
      .groupBy(x => {
        if (x % 2 == 0) "偶数"
        else "奇数"
      })
      .collect()
      .foreach(println)

    val datas2 = Array("dog", "tiger", "lion", "cat", "spider", "eagle")
    sc.parallelize(datas2)
      .keyBy(_.length)
      .groupByKey()
      .collect()
      .foreach(println)
  }

  def aggregateByKey(sc: SparkContext): Unit = {

    // 合并在同一个partition中的值，a的数据类型为zeroValue的数据类型，b的数据类型为原value的数据类型
    def seq(a: Int, b: Int): Int = {
      println("seq: " + a + "\t" + b)
      math.max(a, b)
    }

    // 合并在不同partition中的值，a,b的数据类型为zeroValue的数据类型
    def comb(a: Int, b: Int): Int = {
      println("comb: " + a + "\t" + b)
      a + b
    }

    // 数据拆分成两个分区
    // 分区一数据: (1,3) (1,2)
    // 分区二数据: (1,4) (2,3)
    // zeroValue 中立值，定义返回value的类型，并参与运算
    // seqOp 用来在一个partition中合并值的
    // 分区一相同key的数据进行合并
    // seq: 0	3  (1,3)开始和中位值合并为3
    // seq: 3   2  (1,2)再次合并为3
    // 分区二相同key的数据进行合并
    // seq: 0	4  (1,4)开始和中位值合并为4
    // seq: 0	3  (2,3)开始和中位值合并为3
    // comb 用来在不同partition中合并值的
    // 将两个分区的结果进行合并
    // key为1的, 两个分区都有, 合并为(1,7)
    // key为2的, 只有一个分区有, 不需要合并(2,3)
    sc.parallelize(List((1, 3), (1, 2), (1, 4), (2, 3)), 2)
      .aggregateByKey(0)(seq, comb)
      .collect()
      .foreach(println)
  }

  def sortByKey(sc: SparkContext): Unit = {
    sc.parallelize(Array(60, 70, 80, 55, 45, 75))
      .sortBy(v => v, false)
      .foreach(println)

    sc.parallelize(List((3, 3), (2, 2), (1, 4), (2, 3)))
      .sortByKey(true)
      .foreach(println)
  }

  def join(sc: SparkContext): Unit = {
    sc.parallelize(List((1, "苹果"), (2, "梨"), (3, "香蕉"), (4, "石榴")))
      .join(sc.parallelize(List((1, 7), (2, 3), (3, 8), (4, 3), (5, 9))))
      .foreach(println)
  }

  def cogroup(sc: SparkContext): Unit = {
    val datas1 = List((1, "苹果"),
      (2, "梨"),
      (3, "香蕉"),
      (4, "石榴"))

    val datas2 = List((1, 7),
      (2, 3),
      (3, 8),
      (4, 3))


    val datas3 = List((1, "7"),
      (2, "3"),
      (3, "8"),
      (4, "3"),
      (4, "4"),
      (4, "5"),
      (4, "6"))

    sc.parallelize(datas1)
      .cogroup(sc.parallelize(datas2),
        sc.parallelize(datas3))
      .foreach(println)
  }

  def pipe(sc: SparkContext): Unit = {
    val data = List("hi", "hello", "how", "are", "you")
    sc.makeRDD(data)
      .pipe("/Users/zhangws/echo.sh")
      .collect()
      .foreach(println)
  }

  def coalesce(sc: SparkContext): Unit = {
    val datas = List("hi", "hello", "how", "are", "you")
    val datasRDD = sc.parallelize(datas, 4)
    println("RDD的分区数: " + datasRDD.partitions.length)
    val datasRDD2 = datasRDD.coalesce(2)
    println("RDD的分区数: " + datasRDD2.partitions.length)
  }

  /**
    * 笛卡尔算子
    */
  def cartesian(sc: SparkContext): Unit = {
    val namesRDD = sc.parallelize(Array("张三", "李四", "王五"))
    val scoreRDD = sc.parallelize(Array(60, 70, 80))

    namesRDD.cartesian(scoreRDD)
      .foreach(println)
  }

  def repartitionAndSortWithinPartitions(sc: SparkContext): Unit = {

    def partitionFunc(key: String): Int = {
      key.substring(7).toInt
    }

    val datas = new Array[String](1000)
    val random = new Random(1)
    for (i <- 0 until 10; j <- 0 until 100) {
      val index: Int = i * 100 + j
      datas(index) = "product" + random.nextInt(10) + ",url" + random.nextInt(100)
    }
    val datasRDD = sc.parallelize(datas)
    val pairRDD = datasRDD.map(line => (line, 1))
      .reduceByKey((a, b) => a + b)
    //            .foreach(println)

    pairRDD.repartitionAndSortWithinPartitions(new Partitioner() {
      override def numPartitions: Int = 10

      override def getPartition(key: Any): Int = {
        val str = String.valueOf(key)
        str.substring(7, str.indexOf(',')).toInt
      }
    }).foreach(println)
  }

  def reduce(sc: SparkContext): Unit = {
    println(sc.parallelize(Array(("A", 1), ("B", 6), ("A", 2), ("C", 1), ("A", 7), ("A", 8)))
      .countByKey())
  }
}
