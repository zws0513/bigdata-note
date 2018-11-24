package com.zw.bigdata.spark.ml.classify

import org.apache.spark.ml.classification.NaiveBayes
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.{HashingTF, IDF, LabeledPoint, Tokenizer}
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.sql.Row
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 根据自定数据集训练分类模型
  *
  * Created by zhangws on 17/4/28.
  */
object NaiveBayesApp {

  // rate: 评分; text: 分词后的文本,以空格各个词
  case class RawDataRecord(rate: String, text: String)

  def main(args: Array[String]) {

    if (args.length < 3) {
      println("Usage: trainning-data-directory filter-data-file-path model-output-file-path")
      System.exit(1)
    }

    val trainingDataDir = args(0)
    val filterDataFile = args(1)
    val modelFile = args(2)

    val conf = new SparkConf()
      .setAppName("TestNaiveBayes")
      .setMaster("local[*]")
    val sc = new SparkContext(conf)

    val sqlContext = new org.apache.spark.sql.SQLContext(sc)
    import sqlContext.implicits._

    val filterSet = sc.textFile(filterDataFile).flatMap(_.split(" ")).collect().toSet

    // 加载训练数据集
//    val srcRDD = sc.textFile("/Users/zhangws/Documents/doc/02.study/02.spark/spark/code/classify/data/sougou-train").map {
    val srcRDD = sc.textFile(trainingDataDir).map {
      x =>
        val data = x.split(",")
//        RawDataRecord(data(0), data(1))
        RawDataRecord(data(0), filterStopWords(data(1), filterSet))
    }

    // 将词语转换成数组
    val tokenizer = new Tokenizer().setInputCol("text").setOutputCol("words")
    val wordsData = tokenizer.transform(srcRDD.toDF)
    wordsData.select($"text", $"words").show()

    // 计算每个词在文档中的词频
    val hashingTF = new HashingTF().setNumFeatures(500000).setInputCol("words").setOutputCol("rawFeatures")
    val featurizedData = hashingTF.transform(wordsData)
    featurizedData.show()

    // 计算每个词的TF-IDF
    val idf = new IDF().setInputCol("rawFeatures").setOutputCol("features")
    val idfModel = idf.fit(featurizedData)
    val rescaledData = idfModel.transform(featurizedData)

    // 转换成Bayes的输入格式
    val dataDS = rescaledData.select($"rate", $"features").map {
      case Row(label: String, features: Vector) =>
        LabeledPoint(label.toDouble, features.toDense)
    }

    // 70%作为训练数据，30%作为测试数据
    val splits = dataDS.randomSplit(Array(0.7, 0.3))

    val trainingDF = splits(0).toDF()
    val testDF = splits(1).toDF()

    // 训练模型
    val model = new NaiveBayes().fit(trainingDF)

    val predictions = model.transform(testDF)
//    predictions.show(false)

    // Select (prediction, true label) and compute test error
    val evaluator = new MulticlassClassificationEvaluator()
      .setLabelCol("label")
      .setPredictionCol("prediction")
      .setMetricName("accuracy")
    val accuracy = evaluator.evaluate(predictions)
    println("Test set accuracy = " + accuracy)

//    model.save("file:////Users/zhangws/Documents/doc/02.study/02.spark/spark/code/classify/model/nbmodel")
    model.save(modelFile)
  }

  def filterStopWords(line: String, stopwords: Set[String]): String = {
    val words = line.split(" ")
    words.filter(v => !stopwords.contains(v)).mkString(" ")
  }
}
