package com.zw.storm.helloworld;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;

/**
 * mvn compile exec:java -Dexec.classpathScope=compile -Dexec.mainClass=com.zw.storm.helloworld.HelloWorldTopology
 * Created by zhangws on 16/10/4.
 */
public class HelloWorldTopology {

    public static void main(String[] args) throws Exception {
        TopologyBuilder builder = new TopologyBuilder();
        // 设置喷发节点并分配并发数，该并发数将会控制该对象在集群中的线程数。
        builder.setSpout("randomHelloWorld", new HelloWorldSpout(), 1);
        // 设置数据处理节点并分配并发数。指定该节点接收喷发节点的策略为随机方式。
        builder.setBolt("HelloWorldBolt", new HelloWorldBolt(), 2)
                .shuffleGrouping("randomHelloWorld");

        Config config = new Config();
        config.setDebug(true);

        if (args != null && args.length > 0) {
            config.setNumWorkers(1);
            StormSubmitter.submitTopology(args[0], config, builder.createTopology());
        } else {
            // 这里是本地模式下运行的启动代码。
            config.setMaxTaskParallelism(1);
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("test", config, builder.createTopology());
            Utils.sleep(10000);
            cluster.killTopology("test");
            cluster.shutdown();
        }
    }
}
