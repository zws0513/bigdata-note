#!/usr/bin/env python
# -*- coding: utf-8 -*-


from kafka import KafkaProducer, KafkaConsumer
from kafka.errors import KafkaError


class KafkaProducerEx:
    """使用kafka的生产模块

    """

    def __init__(self, host, port, topic):
        self.host = host
        self.port = port
        self.topic = topic
        self.producer = KafkaProducer(bootstrap_servers='{kafka_host}:{kafka_port}'.format(
            kafka_host=self.host,
            kafka_port=self.port
        ))

    def send(self, message):
        try:
            producer = self.producer
            producer.send(self.topic, message.encode('utf-8'))
            producer.flush()
        except KafkaError as e:
            print e


class KafkaConsumerEx:
    """使用Kafka—python的消费模块

    """

    def __init__(self, host, port, topic, groupid):
        self.host = host
        self.port = port
        self.topic = topic
        self.groupid = groupid
        self.consumer = KafkaConsumer(self.topic, group_id=self.groupid,
                                      bootstrap_servers='{kafka_host}:{kafka_port}'.format(
                                          kafka_host=self.host,
                                          kafka_port=self.port))

    def consume(self):
        try:
            for message in self.consumer:
                # print json.loads(message.value)
                yield message
        except KeyboardInterrupt, e:
            print e


def main():
    """
    测试consumer和producer
    :return:
    """

    # 测试生产模块
    # producer = KafkaProducerEx("slave3.test", 9092, "impalaLog")
    # with open("/Users/Documents/impala_info.log") as f:
    #     for line in f:
    #         producer.send(line.strip('\n'))

    # 测试生产模块
    # producer = KafkaProducerEx("slave3.test", 9092, "hiveLog")
    # with open("/Users/Documents/hive_info.log") as f:
    #     for line in f:
    #         producer.send(line.strip('\n'))

    # 测试消费模块
    #消费模块的返回格式为ConsumerRecord(topic=u'ranktest', partition=0, offset=202, timestamp=None,
    #\timestamp_type=None, key=None, value='"{abetst}:{null}---0"', checksum=-1868164195,
    #\serialized_key_size=-1, serialized_value_size=21)

    consumer = KafkaConsumerEx('slave3.test', 9092, "impalaLog", 'test-python-ranktest')
    message = consumer.consume()
    for i in message:
        print i.value


if __name__ == '__main__':
    main()
