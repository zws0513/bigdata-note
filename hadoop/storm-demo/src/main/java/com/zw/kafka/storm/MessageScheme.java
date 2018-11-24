package com.zw.kafka.storm;

import backtype.storm.spout.Scheme;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * 对kafka出来的数据转换成字符串
 * <p>
 *     KafkaSpout是Storm中自带的Spout,
 *     使用KafkaSpout时需要子集实现Scheme接口，它主要负责从消息流中解析出需要的数据
 * </p>
 *
 * Created by zhangws on 16/10/2.
 */
public class MessageScheme implements Scheme {

    public List<Object> deserialize(byte[] bytes) {
        try {
            String msg = new String(bytes, "UTF-8");
            return new Values(msg);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Fields getOutputFields() {
        return new Fields("msg");
    }
}
