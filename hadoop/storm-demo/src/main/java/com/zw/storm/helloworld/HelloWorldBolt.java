package com.zw.storm.helloworld;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;

/**
 * 接收喷发节点(Spout)发送的数据进行简单的处理后，发射出去。
 * <p>
 *     用于读取已产生的Tuple并实现必要的统计逻辑
 * </p>
 *
 * Created by zhangws on 16/10/4.
 */
public class HelloWorldBolt extends BaseBasicBolt {

    private int myCount;

    public void execute(Tuple tuple, BasicOutputCollector collector) {
        String test = tuple.getStringByField("sentence");
        if ("Hello World".equals(test)) {
            myCount++;
            System.out.println("==========================: " + myCount);
        } else {
            //System.out.println("++++++++++++++++++++++++++: " + myCount);
        }
    }

    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }
}
