package com.zw.bigdata.spark.ml.classify

import org.apache.spark.ml.classification.NaiveBayesModel
import org.apache.spark.ml.feature.{LabeledPoint, IDF, HashingTF, Tokenizer}
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.sql.Row
import org.apache.spark.{SparkContext, SparkConf}

/**
  * 使用分类模型
  *
  * Created by zhangws on 17/4/30.
  */
object ApplyModel {

  // text: 分词后的文本,以空格各个词
  case class RawDataRecord(label: String, text: String)

  def main(args: Array[String]) {

    val modelFile = args(0)
    val filterDataFile = args(1)
    val waitPredictionFile = args(2)

    val conf = new SparkConf().setAppName("TestNaiveBayes").setMaster("local[*]")
    val sc = new SparkContext(conf)

    val sqlContext = new org.apache.spark.sql.SQLContext(sc)
    import sqlContext.implicits._

    val filterSet = sc.textFile(filterDataFile).flatMap(_.split(" ")).collect().toSet

    // 加载待分类数据集
//    val srcRDD = sc.textFile("/Users/zhangws/Documents/doc/02.study/02.spark/spark/code/classify/data/wait-classify").map {
    val srcRDD = sc.textFile(waitPredictionFile).map {
      x =>
        val data = x.split(",")
        RawDataRecord(data(0), filterStopWords(data(1), filterSet))
    }

    // 将词语转换成数组
    val tokenizer = new Tokenizer().setInputCol("text").setOutputCol("words")
    val wordsData = tokenizer.transform(srcRDD.toDF)

    // 计算每个词在文档中的词频
    val hashingTF = new HashingTF().setNumFeatures(500000).setInputCol("words").setOutputCol("rawFeatures")
    val featurizedData = hashingTF.transform(wordsData)

    // 计算每个词的TF-IDF
    val idf = new IDF().setInputCol("rawFeatures").setOutputCol("features")
    val idfModel = idf.fit(featurizedData)
    val rescaledData = idfModel.transform(featurizedData)

    // 转换成Bayes的输入格式
    val dataDS = rescaledData.select($"label", $"features").map {
      case Row(label: String, features: Vector) =>
        LabeledPoint(label.toDouble, features.toDense)
    }

//    val NBmodel = NaiveBayesModel.load("file:////Users/zhangws/Documents/doc/02.study/02.spark/spark/code/classify/model/nbmodel")
    val NBmodel = NaiveBayesModel.load(modelFile)
    val predictions = NBmodel.transform(dataDS)
//    predictions.show(false)

    predictions.select($"label",$"prediction").foreach { x =>
      println(""+x.getAs("label")+" "+x.getAs("prediction")+"\n\r")
    }

  }

  def filterStopWords(line: String, stopwords: Set[String]): String = {
    val words = line.split(" ")
    words.filter(v => !stopwords.contains(v)).mkString(" ")
  }
}
