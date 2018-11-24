package com.zw.storm.helloworld;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;

import java.util.Map;
import java.util.Random;

/**
 * Spout起到和外界沟通的作用，他可以从一个数据库中按照某种规则取数据，也可以从分布式队列中取任务
 * <p>
 *     生成一个随机数生成的Tuple
 * </p>
 *
 * Created by zhangws on 16/10/3.
 */
public class HelloWorldSpout extends BaseRichSpout {

    // 用来发射数据的工具类
    private SpoutOutputCollector collector;

    private int referenceRandom;

    private static final int MAX_RANDOM = 10;

    public HelloWorldSpout() {
        final Random rand = new Random();
        referenceRandom = rand.nextInt(MAX_RANDOM);
    }

    /**
     * 定义字段id，该id在简单模式下没有用处，但在按照字段分组的模式下有很大的用处。
     * <p>
     *     该declarer变量有很大作用，我们还可以调用declarer.declareStream();
     *     来定义stramId，该id可以用来定义更加复杂的流拓扑结构
     * </p>
     * @param outputFieldsDeclarer
     */
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("sentence"));
    }

    /**
     * 初始化collector
     *
     * @param map
     * @param topologyContext
     * @param spoutOutputCollector
     */
    public void open(Map map, TopologyContext topologyContext,
                     SpoutOutputCollector spoutOutputCollector) {
        this.collector = spoutOutputCollector;
    }

    /**
     * 每调用一次就可以向storm集群中发射一条数据（一个tuple元组），该方法会被不停的调用
     */
    public void nextTuple() {
        Utils.sleep(100);
        final Random rand = new Random();
        int instanceRandom = rand.nextInt(MAX_RANDOM);
        if (instanceRandom == referenceRandom) {
            collector.emit(new Values("Hello World"));
        } else {
            collector.emit(new Values("Other Random Word"));
        }
    }
}
