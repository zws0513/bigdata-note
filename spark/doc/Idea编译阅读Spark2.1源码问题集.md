#flume-sink缺Java文件

把external/flume-sink/target目录中的spark-streaming-flume-sink_2.11-2.1.0-sources.jar（注意：只有编译过Spark才有）解压，复制以下文件到org.apache.spark.streaming.flume.sink包中。

```
EventBatch.java
SparkFlumeProtocol.java
SparkSinkEvent.java
```

#spark-catalyst缺Java文件

把sql/catalyst/target目录中的spark-catalyst_2.11-2.1.0-sources.jar解压，复制以下文件到org.apache.spark.sql.catalyst.parser包中。

```
SqlBase为前缀的6个文件
```

#hive-thriftserver缺Java文件

把sql/hive-thriftserver/target目录中的spark-hive-thriftserver_2.11-2.1.0-sources.jar解压，复制以下文件到org.apache.hive.service.cli.thrift包中。

```
该包下所有java文件
```

#org.apache.spark.SparkException: Error while locating file spark-version-info.properties

core/src/main/resources目录下创建空的spark-version-info.properties文件即可。

#启动Master时，java.lang.NoClassDefFoundError: com/google/common/cache/CacheLoader

spark-core_2.11依赖包guava-14.0.1.jar的scope修改为compile。

#启动Worker的配置

```
# Program arguments参数
--webui-port 8081
spark://master:7077
```