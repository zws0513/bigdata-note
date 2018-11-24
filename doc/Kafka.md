#1. 安装
##1.1 ZooKeeper
###1.1.1 安装

[安装参考](http://blog.csdn.net/u013980127/article/details/52351900#t10)

###1.1.2 集群启动关闭

**配置节点IP或域名(本文存在conf/slaves文件中)**

```
vim conf/slaves

hsm01
hss01
hss02
```

**启动脚本**

```
#!/usr/bin/env bash

# Start all ZooKeeper daemons
# Run this on master node
# Starts a worker on each node specified in conf/slaves

if [ -z "${ZOOKEEPER_HOME}" ]; then
  export ZOOKEEPER_HOME="$(cd "`dirname "$0"`"/..; pwd)"
fi

SLAVE_FILE=${ZOOKEEPER_HOME}/conf/slaves

SLAVE_NAMES=$(cat "$SLAVE_FILE" | sed  's/#.*$//;/^$/d')

for slave in $SLAVE_NAMES ;
do
ssh -T $slave <<EOF
    source ~/.bash_profile
    $ZOOKEEPER_HOME/bin/zkServer.sh start
EOF
echo start $slave ZooKeeper [ done ]
sleep 1
done

echo start all ZooKeeper [ done ]
```

**关闭脚本**

```
#!/usr/bin/env bash

# Stop all ZooKeeper daemons
# Run this on master node
# Stops a worker on each node specified in conf/slaves

if [ -z "${ZOOKEEPER_HOME}" ]; then
  export ZOOKEEPER_HOME="$(cd "`dirname "$0"`"/..; pwd)"
fi

SLAVE_FILE=${ZOOKEEPER_HOME}/conf/slaves

SLAVE_NAMES=$(cat "$SLAVE_FILE" | sed  's/#.*$//;/^$/d')

for slave in $SLAVE_NAMES ;
do
ssh -T $slave <<EOF
    source ~/.bash_profile
    $ZOOKEEPER_HOME/bin/zkServer.sh stop
EOF
echo stop $slave ZooKeeper [ done ]
sleep 1
done

echo stop all ZooKeeper [ done ]
```

<font color=red>
最后别忘了脚本权限、为方便配置的环境变量以及免密码登录等
</font>

##1.2 Kafka
###1.2.1 安装

[安装参考](http://blog.csdn.net/u013980127/article/details/52863714#t1)

###1.2.2 集群启动关闭

**启动脚本**

```bash
# 其他和ZooKeeper一样，只有启动命令不一样而已
for slave in $SLAVE_NAMES ;
do
ssh -T $slave <<EOF
    source ~/.bash_profile
    cd \$KAFKA_HOME
    bin/kafka-server-start.sh -daemon ./config/server.properties
EOF
echo start $slave kafka [ done ]
sleep 1
done
```

**关闭脚本**

```
# 同理
for slave in $SLAVE_NAMES ;
do
ssh -T $slave <<EOF
    source ~/.bash_profile
    cd \$KAFKA_HOME
    bin/kafka-server-stop.sh
EOF
echo stop $slave kafka [ done ]
sleep 1
done
```

#2. 架构原理概述
##2.1 架构
###2.1.1 基本概念

1. Topic：消息存放的目录即主题。
2. Producer：生产消息到topic的一方。
3. Consumer：可以订阅一个或多个话题，并从Broker拉数据，从而消费这些已发布的消息。
4. Broker：已发布的消息保存在一组服务器中，它们被称为代理（Broker）或Kafka集群。

###2.1.2 架构图

![Kafka架构图](http://img.blog.csdn.net/20161116102245638)

##2.2 存储策略

1. kafka以topic来进行消息管理，每个topic包含多个partition，每个partition对应一个逻辑log，有多个segment组成。
2. 每个segment中存储多条消息（见下图），消息id由其逻辑位置决定，即从消息id可直接定位到消息的存储位置，避免id到位置的额外映射。
3. 每个part在内存中对应一个index，记录每个segment中的第一条消息偏移。
4. 发布者发到某个topic的消息会被均匀的分布到多个partition上（或根据用户指定的路由规则进行分布），broker收到发布消息往对应partition的最后一个segment上添加该消息，当某个segment上的消息条数达到配置值或消息发布时间超过阈值时，segment上的消息会被flush到磁盘，只有flush到磁盘上的消息订阅者才能订阅到，segment达到一定的大小后将不会再往该segment写数据，broker会创建新的segment。

![存储策略](http://img.blog.csdn.net/20161116103244876)

##2.3 消息可靠性

在实际消息传递过程中，可能会出现如下三中情况：

1. at most once: 最多一次, 这个和JMS中"非持久化"消息类似. 发送一次, 无论成败,将不会重发.
2. at least once: 消息至少发送一次, 如果消息未能接受成功, 可能会重发, 直到接收成功.
3. exactly once: 消息只会发送一次.

**Producer端：**Kafka是这么处理的，当一个消息被发送后，Producer会等待broker成功接收到消息的反馈（可通过参数控制等待时间），如果消息在途中丢失或是其中一个broker挂掉，Producer会重新发送（我们知道Kafka有备份机制，可以通过参数控制是否等待所有备份节点都收到消息）。 

**Consumer端：**前面讲到过partition，broker端记录了partition中的一个offset值，这个值指向Consumer下一个即将消费message。当Consumer收到了消息，但却在处理过程中挂掉，此时Consumer可以通过这个offset值重新找到上一个消息再进行处理。Consumer还有权限控制这个offset值，对持久化到broker端的消息做任意处理。

##2.4 复制备份

1. kafka将每个partition数据复制到多个server上,任何一个partition有一个leader和多个follower(可以没有)
2. 备份的个数可以通过broker配置文件来设定
3. leader处理所有的read-write请求,follower需要和leader保持同步
4. Follower和consumer一样,消费消息并保存在本地日志中
5. leader负责跟踪所有的follower状态,如果follower “落后”太多或 者失效,leader将会把它从replicas同步列表中删除
6. 当所有的follower都将一条消息保存成功,此消息才被认为是 “committed”,那么此时consumer才能消费它
7. 即使只有一个replicas实例存活,仍然可以保证消息的正常发送和接 收,只要zookeeper集群正常即可

##2.5 主要配置

###2.5.1 Broker配置（server.properties）

属性 | 默认值 | 描述
---------- | ----------- | ----------
broker.id |  | 必填参数，broker的唯一标识
log.dirs | /tmp/kafka-logs | Kafka数据存放的目录。可以指定多个目录，中间用逗号分隔，当新partition被创建的时会被存放到当前存放partition最少的目录。
port | 9092 | BrokerServer接受客户端连接的端口号
zookeeper.connect |  | Zookeeper的连接串，格式为：hostname1:port1,hostname2:port2,hostname3:port3。可以填一个或多个，为了提高可靠性，建议都填上。注意，此配置允许我们指定一个zookeeper路径来存放此kafka集群的所有数据，为了与其他应用集群区分开，建议在此配置中指定本集群存放目录，格式为：hostname1:port1,hostname2:port2,hostname3:port3/chroot/path 。需要注意的是，消费者的参数要和此参数一致。
message.max.bytes | 1000000 | 服务器可以接收到的最大的消息大小。注意此参数要和consumer的maximum.message.size大小一致，否则会因为生产者生产的消息太大导致消费者无法消费。
num.io.threads | 8 | 服务器用来执行读写请求的IO线程数，此参数的数量至少要等于服务器上磁盘的数量。
queued.max.requests | 500 | I/O线程可以处理请求的队列大小，若实际请求数超过此大小，网络线程将停止接收新的请求。
socket.send.buffer.bytes | 100 * 1024 | The SO_SNDBUFF buffer the server prefers for socket connections.
socket.receive.buffer.bytes | 100 * 1024 | The SO_RCVBUFF buffer the server prefers for socket connections.
socket.request.max.bytes | 100 * 1024 * 1024 | 服务器允许请求的最大值， 用来防止内存溢出，其值应该小于 Java heap size.
num.partitions | 1 | 默认partition数量，如果topic在创建时没有指定partition数量，默认使用此值，建议改为5
log.segment.bytes | 1024 * 1024 * 1024 | Segment文件的大小，超过此值将会自动新建一个segment，此值可以被topic级别的参数覆盖。
log.roll.{ms,hours} | 24 * 7 hours | 新建segment文件的时间，此值可以被topic级别的参数覆盖。
log.retention.{ms,minutes,hours} | 7 days | Kafka segment log的保存周期，保存周期超过此时间日志就会被删除。此参数可以被topic级别参数覆盖。数据量大时，建议减小此值。
log.retention.bytes | -1 | 每个partition的最大容量，若数据量超过此值，partition数据将会被删除。注意这个参数控制的是每个partition而不是topic。此参数可以被log级别参数覆盖。
log.retention.check.interval.ms | 5 minutes | 删除策略的检查周期
auto.create.topics.enable | true | 自动创建topic参数，建议此值设置为false，严格控制topic管理，防止生产者错写topic。
default.replication.factor | 1 | 默认副本数量，建议改为2。
replica.lag.time.max.ms | 10000 | 在此窗口时间内没有收到follower的fetch请求，leader会将其从ISR(in-sync replicas)中移除。
replica.lag.max.messages | 4000 | 如果replica节点落后leader节点此值大小的消息数量，leader节点就会将其从ISR中移除。
replica.socket.timeout.ms | 30 * 1000 | replica向leader发送请求的超时时间。
zookeeper.session.timeout.ms | 6000 | ZooKeeper session 超时时间。如果在此时间内server没有向zookeeper发送心跳，zookeeper就会认为此节点已挂掉。 此值太低导致节点容易被标记死亡；若太高，.会导致太迟发现节点死亡。
zookeeper.connection.timeout.ms | 6000 | 客户端连接zookeeper的超时时间。
zookeeper.sync.time.ms | 2000 | ZK follower落后 ZK leader的时间。
controlled.shutdown.enable | true | 允许broker shutdown。如果启用，broker在关闭自己之前会把它上面的所有leaders转移到其它brokers上，建议启用，增加集群稳定性。
delete.topic.enable | false | 启用delete topic参数，建议设置为true。

###2.5.2 Producer配置

属性 | 默认值 | 描述
---------- | ----------- | ----------
metadata.broker.list | | 启动时producer查询brokers的列表，可以是集群中所有brokers的一个子集。注意，这个参数只是用来获取topic的元信息用，producer会从元信息中挑选合适的broker并与之建立socket连接。格式是：host1:port1,host2:port2。
request.required.acks | 0 | producer要求leader partition 收到确认的副本个数。0：表示producer不会等待broker的响应；1：表示producer会在leader partition收到消息时得到broker的一个确认；-1：producer会在所有备份的partition收到消息时得到broker的确认。
request.timeout.ms | 10000 | Broker等待ack的超时时间，若等待时间超过此值，会返回客户端错误信息。
producer.type | sync | 同步异步模式。async表示异步，sync表示同步。如果设置成异步模式，可以允许生产者以batch的形式push数据，这样会极大的提高broker性能，推荐设置为异步。
serializer.class | kafka.serializer.DefaultEncoder | 序列号类，.默认序列化成 byte[] 。
key.serializer.class |  | Key的序列化类，默认同上。
partitioner.class | kafka.producer.DefaultPartitioner | Partition类，默认对key进行hash。
compression.codec | none | 指定producer消息的压缩格式，可选参数为： “none”, “gzip” and “snappy”
compressed.topics | null | 启用压缩的topic名称。若上面参数选择了一个压缩格式，那么压缩仅对本参数指定的topic有效，若本参数为空，则对所有topic有效。
message.send.max.retries | 3 | Producer发送失败时重试次数。若网络出现问题，可能会导致不断重试。
queue.buffering.max.ms | 5000 | 启用异步模式时，producer缓存消息的时间。比如我们设置成1000时，它会缓存1秒的数据再一次发送出去，这样可以极大的增加broker吞吐量，但也会造成时效性的降低。
queue.buffering.max.messages | 10000 | 采用异步模式时producer buffer 队列里最大缓存的消息数量，如果超过这个数值，producer就会阻塞或者丢掉消息。
queue.enqueue.timeout.ms | -1 | 当达到上面参数值时producer阻塞等待的时间。如果值设置为0，buffer队列满时producer不会阻塞，消息直接被丢掉。若值设置为-1，producer会被阻塞，不会丢消息。
batch.num.messages | 200 | 采用异步模式时，一个batch缓存的消息数量。达到这个数量值时producer才会发送消息。

###2.5.3 Consumer配置

属性 | 默认值 | 描述
---------- | ----------- | ----------
group.id | | Consumer的组ID，相同goup.id的consumer属于同一个组。
zookeeper.connect |  | Consumer的zookeeper连接串，要和broker的配置一致。
consumer.id | | 如果不设置会自动生成。
socket.timeout.ms | 30 * 1000 | 网络请求的socket超时时间。实际超时时间由max.fetch.wait + socket.timeout.ms 确定。
socket.receive.buffer.bytes | 64 * 1024 | The socket receive buffer for network requests.
fetch.message.max.bytes | 1024 * 1024 | 查询topic-partition时允许的最大消息大小。consumer会为每个partition缓存此大小的消息到内存，因此，这个参数可以控制consumer的内存使用量。这个值应该至少比server允许的最大消息大小大，以免producer发送的消息大于consumer允许的消息。
num.consumer.fetchers | 1 | The number fetcher threads used to fetch data.
auto.commit.enable | true | 如果此值设置为true，consumer会周期性的把当前消费的offset值保存到zookeeper。当consumer失败重启之后将会使用此值作为新开始消费的值。
auto.commit.interval.ms | 60 * 1000 | Consumer提交offset值到zookeeper的周期。
queued.max.message.chunks | 2 | 用来被consumer消费的message chunks 数量， 每个chunk可以缓存fetch.message.max.bytes大小的数据量。
consumer.timeout.ms | -1 | 若在指定时间内没有消息消费，consumer将会抛出异常。

##2.6 参考

[Kafka基本原理](http://www.cnblogs.com/Leo_wl/p/5493995.html)

[分布式消息系统 Kafka 简介](https://my.oschina.net/leejun2005/blog/304145)

[Apache kafka 工作原理介绍](http://www.ibm.com/developerworks/cn/opensource/os-cn-kafka/index.html)

[Kafka深度解析](http://www.cnblogs.com/coprince/p/5893066.html)

[Kafka 设计与原理详解](http://blog.csdn.net/suifeng3051/article/details/48053965)

#3. 命令

```
# 启动
nohup ./bin/kafka-server-start.sh config/server.properties > /dev/null 2>&1 &

# 创建topic
kafka-topics.sh --zookeeper hsm01:2181/kafka --create --replication-factor 1 --partitions 1 --topic user_events

# 查看topic列表
kafka-topics.sh --zookeeper hsm01:2181/kafka --list

# 查看主题详情
kafka-topics.sh --zookeeper hsm01:2181/kafka --describe --topic user_events

# 删除主题
kafka-topics.sh --zookeeper hsm01:2181/kafka --delete --topic user_events

# 创建一个broker，发布者
kafka-console-producer.sh --broker-list hsm01:9092 --topic user_events

# 创建一个订阅者
kafka-console-consumer.sh --zookeeper hsm01:2181/kafka --topic user_events --from-beginning
```

#4. Java开发
##4.1 生产者
##4.2 消费者