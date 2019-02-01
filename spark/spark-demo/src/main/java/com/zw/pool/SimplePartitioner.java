package com.zw.pool;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

import java.util.Map;

/**
 * Created by zhangws on 2017/10/12.
 */
public class SimplePartitioner implements Partitioner {

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        if (key == null) {
            return 0;
        } else {
            String k = (String) key;
            return Math.abs(k.hashCode()) % cluster.partitionsForTopic(topic).size();
        }
    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> configs) {

    }
}
