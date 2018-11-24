package com.zw.kafka.storm;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by zhangws on 16/10/2.
 */
public class DataProducerInsert {

    private static Producer<Integer,String> producer;
    private final Properties props=new Properties();
    public DataProducerInsert(){
        //定义连接的broker list
        props.put("metadata.broker.list", "192.168.99.145:9092");
        //定义序列化类 Java中对象传输之前要序列化
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        //props.put("advertised.host.name", "192.168.1.216");
        producer = new Producer<Integer, String>(new ProducerConfig(props));
    }
    public static void main(String[] args) {
        DataProducerInsert sp=new DataProducerInsert();
        //定义topic
        String topic="topic1";
        //开始时间统计
        long startTime = System.currentTimeMillis();
        //定义要发送给topic的消息
        String messageStr = "This is a message";
        List<KeyedMessage<Integer, String>> datalist = new ArrayList<KeyedMessage<Integer, String>>();

        //构建消息对象
        KeyedMessage<Integer, String> data = new KeyedMessage<Integer, String>(topic, messageStr);
        datalist.add(data);

        //结束时间统计
        long endTime = System.currentTimeMillis();
        KeyedMessage<Integer, String> data1 = new KeyedMessage<Integer, String>(topic, "用时" + (endTime-startTime)/1000.0);
        datalist.add(data1);

        //推送消息到broker
        producer.send(data);
        producer.close();
    }
}
