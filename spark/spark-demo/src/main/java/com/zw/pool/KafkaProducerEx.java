package com.zw.pool;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.Serializable;
import java.util.Properties;

/**
 * Created by zhangws on 2019/2/1.
 */
public class KafkaProducerEx  implements Serializable {

    private static final String KEY_CLASS = "org.apache.kafka.common.serialization.StringSerializer";

    private static final String VALUE_CLASS = "org.apache.kafka.common.serialization.ByteArraySerializer";

    private static final String PARTITIONER_CLASS = "cn.gitv.lactea.etl.common.factory.SimplePartitioner";

    private static KafkaProducerEx instance = null;

    private Producer<String, byte[]> producer = null;

    private KafkaProducerEx(String brokers) {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KEY_CLASS);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, VALUE_CLASS);
        properties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, PARTITIONER_CLASS);
        this.producer = new KafkaProducer<>(properties);
    }

    public static synchronized KafkaProducerEx getInstance(String brokers) {
        if (instance == null) {
            instance = new KafkaProducerEx(brokers);
        }
        return instance;
    }

    // 单条发送
    public void send(ProducerRecord<String, byte[]> producerRecord) {
        producer.send(producerRecord);
    }

    public void close() {
        producer.close();
    }
}
