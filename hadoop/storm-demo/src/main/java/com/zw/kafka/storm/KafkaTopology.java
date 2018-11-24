package com.zw.kafka.storm;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;
import storm.kafka.BrokerHosts;
import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.ZkHosts;
import storm.kafka.bolt.KafkaBolt;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置kafka提交topology到storm的代码
 * <p>
 * topic1的含义kafka接收生产者过来的数据所需要的topic;
 * topic2是KafkaBolt也就是storm中的bolt生成的topic，当然这里topic2这行配置可以省略，
 * 是没有任何问题的，类似于一个中转的东西
 * </p>
 * Created by zhangws on 16/10/2.
 */
public class KafkaTopology {

    private static final String BROKER_ZK_LIST = "hsm01:2181,hss01:2181,hss02:2181";
    private static final String ZK_PATH = "/kafka/brokers";

    public static void main(String[] args) throws Exception {
        // 配置Zookeeper地址
        BrokerHosts brokerHosts = new ZkHosts(BROKER_ZK_LIST, ZK_PATH);
        // 配置Kafka订阅的Topic，以及zookeeper中数据节点目录和名字
        SpoutConfig spoutConfig = new SpoutConfig(brokerHosts, "sogolog", "/kafka", "kafka");

        // 配置KafkaBolt中的kafka.broker.properties
        Config conf = new Config();
        Map<String, String> map = new HashMap<String, String>();

        // 配置Kafka broker地址
        map.put("metadata.broker.list", "hsm01:9092");
        // serializer.class为消息的序列化类
        map.put("serializer.class", "kafka.serializer.StringEncoder");
        conf.put("kafka.broker.properties", map);
        // 配置KafkaBolt生成的topic
        conf.put("topic", "topic2");

        spoutConfig.scheme = new SchemeAsMultiScheme(new MessageScheme());
//        spoutConfig.scheme = new SchemeAsMultiScheme(new StringScheme());

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("kafka-spout", new KafkaSpout(spoutConfig), 1);
        builder.setBolt("kafka-bolt", new SequenceBolt()).shuffleGrouping("kafka-spout");
        builder.setBolt("kafka-bolt2", new KafkaBolt<String, Integer>()).shuffleGrouping("kafka-bolt");

        String name = KafkaTopology.class.getSimpleName();
        if (args != null && args.length > 0) {
            // Nimbus host name passed from command line
            conf.put(Config.NIMBUS_HOST, args[0]);
            conf.setNumWorkers(2);
            StormSubmitter.submitTopology(name, conf, builder.createTopology());
        } else {
            //本地模式运行
            conf.setMaxTaskParallelism(3);
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology(name, conf, builder.createTopology());
            Utils.sleep(60000);
            cluster.killTopology(name);
            cluster.shutdown();
        }
    }
}
