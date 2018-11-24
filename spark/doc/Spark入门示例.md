**pom.xml**

```
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <parent>
        <artifactId>bigdata-demo</artifactId>
        <groupId>com.zw</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>
    <artifactId>spark-demo</artifactId>
    <inceptionYear>2008</inceptionYear>
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <java.version>1.8</java.version>
        <scala.version>2.11.8</scala.version>
        <spark.version>2.0.1</spark.version>
        <hadoop.version>2.6.4</hadoop.version>
    </properties>

    <repositories>
        <repository>
            <id>scala-tools.org</id>
            <name>Scala-Tools Maven2 Repository</name>
            <url>http://scala-tools.org/repo-releases</url>
        </repository>
    </repositories>

    <pluginRepositories>
        <pluginRepository>
            <id>scala-tools.org</id>
            <name>Scala-Tools Maven2 Repository</name>
            <url>http://scala-tools.org/repo-releases</url>
        </pluginRepository>
    </pluginRepositories>

    <dependencies>
        <dependency>
            <groupId>org.scala-lang</groupId>
            <artifactId>scala-library</artifactId>
            <version>${scala.version}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.spark</groupId>
            <artifactId>spark-core_2.11</artifactId>
            <version>${spark.version}</version>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.4</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.specs</groupId>
            <artifactId>specs</artifactId>
            <version>1.2.5</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <sourceDirectory>src/main/scala</sourceDirectory>
        <testSourceDirectory>src/test/scala</testSourceDirectory>
        <plugins>
            <plugin>
                <groupId>org.scala-tools</groupId>
                <artifactId>maven-scala-plugin</artifactId>
                <!-- <version>2.15.1</version> -->
                <executions>
                    <execution>
                        <goals>
                            <goal>compile</goal>
                            <goal>testCompile</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <scalaVersion>${scala.version}</scalaVersion>
                    <args>
                        <arg>-target:jvm-1.8</arg>
                    </args>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-eclipse-plugin</artifactId>
                <configuration>
                    <downloadSources>true</downloadSources>
                    <buildcommands>
                        <buildcommand>ch.epfl.lamp.sdt.core.scalabuilder</buildcommand>
                    </buildcommands>
                    <additionalProjectnatures>
                        <projectnature>ch.epfl.lamp.sdt.core.scalanature</projectnature>
                    </additionalProjectnatures>
                    <classpathContainers>
                        <classpathContainer>org.eclipse.jdt.launching.JRE_CONTAINER</classpathContainer>
                        <classpathContainer>ch.epfl.lamp.sdt.launching.SCALA_CONTAINER</classpathContainer>
                    </classpathContainers>
                </configuration>
            </plugin>
        </plugins>
    </build>
    <reporting>
        <plugins>
            <plugin>
                <groupId>org.scala-tools</groupId>
                <artifactId>maven-scala-plugin</artifactId>
                <configuration>
                    <scalaVersion>${scala.version}</scalaVersion>
                </configuration>
            </plugin>
        </plugins>
    </reporting>
</project>
```

**ScalaWordCount.scala**

```
package com.zw.spark.demo

import org.apache.spark.{SparkContext, SparkConf}

/**
  * 统计单词个数
  * 
  * Created by zhangws on 16/10/21.
  */
object ScalaWordCount {
  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName(ScalaWordCount.getClass.getSimpleName)
    val sc = new SparkContext(conf)
    val textFile = sc.textFile("file:///home/zkpk/spark-2.0.1/README.md")
    val words = textFile.flatMap(line => line.split(" "))
    val wordPairs = words.map(word => (word, 1))
    val wordCounts = wordPairs.reduceByKey((a,b) => a + b)
    println("wordCounts: ")
    wordCounts.collect().foreach(println)
  }
}
```

**本地运行**

![本地运行](http://img.blog.csdn.net/20161022112946835)

**集群运行**

语法：

```
./bin/spark-submit \
  --class <main-class> \
  --master <master-url> \
  --deploy-mode <deploy-mode> \
  --conf <key>=<value> \
  --jars <dependence-jars> \
  <application-jar> \
  [application-arguments]
  
# 说明
--class：指定包中的object对象；
--master：指定Spark集群地址，可以是下面的任意一种，

  1. local[N] 表示本地模式
  2. spark://host:port 表示Standalone模式
  3. yarn
  4. mesos://host:port

--deploy-mode：运行模式，可选client或cluster
--conf：配置文件-》命令行参数-》程序中SparkConf对象的优先级
--jars：指定额外的依赖包，集群上的所有节点都会访问这些文件

程序中的URL规则有3类：

  1. 以file:/开头的本地文件，其他节点会通过Spark程序启动的http服务来下载
  2. hdfs:、http:、https:、ftp，节点直接访问这些服务
  3. 以local:/开头的文件，表示文件在本地，各节点直接在本地的该路径下访问，没有网络IO。
```

集群运行示例

```
spark-submit \
--class com.zw.spark.demo.ScalaWordCount \
--master yarn \
--deploy-mode cluster \
spark-demo-1.0-SNAPSHOT.jar
```