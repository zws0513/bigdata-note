#1. 收集日志文件到Kafka

流程：

业务系统 => 日志文件 => FileBeat => Logstash => Kafka

##1.1 Kafka

```
# 查看主题
kafka-topics.sh --zookeeper host-10-18-16-8:2181/kafka --list

# 创建Topic
kafka-topics.sh --create --zookeeper host-10-18-16-8:2181/kafka --replication-factor 1 --partitions 1 --topic p4xlogstash

# 创建一个订阅者
kafka-console-consumer.sh --zookeeper host-10-18-16-8:2181/kafka --topic p4xlogstash --from-beginning

# 删除主题
kafka-topics.sh --zookeeper host-10-18-16-8:2181/kafka --delete --topic p4xlogstash
```

##1.2 Logstash的output

```
output {
    # 过滤数据（info字段以字符串"tag"开头）
    if [info] =~ /^tag/ {
        kafka {
            topic_id => "p4xlogstash"
            bootstrap_servers => "host-10-18-16-8:9092,host-10-18-16-28:9092,host-10-18-16-29:9092"
            codec => plain {
                format => "%{message}"
            }
        }
        stdout {
            codec => rubydebug
        }
    }
}
```

##1.3 FileBeat

####1.3.1 生成FileBeat与Logstash之间的SSL认证

```
openssl req -subj '/CN=host-10-18-16-8/' -x509 -days $((100*365)) -batch -nodes -newkey rsa:2048 -keyout ~/filebeat-5.2.1/pki/certs/filebeat.key -out ~/filebeat-5.2.1/pki/certs/filebeat.crt

openssl req -subj '/CN=host-10-18-16-27/' -x509 -days $((100*365)) -batch -nodes -newkey rsa:2048 -keyout ~/filebeat-5.2.1/pki/certs/filebeat.key -out ~/filebeat-5.2.1/pki/certs/filebeat.crt

openssl req -subj '/CN=host-10-18-16-26/' -x509 -days $((100*365)) -batch -nodes -newkey rsa:2048 -keyout ~/filebeat-5.2.1/pki/certs/filebeat.key -out ~/filebeat-5.2.1/pki/certs/filebeat.crt

openssl req -subj '/CN=host-10-18-16-28/' -x509 -days $((100*365)) -batch -nodes -newkey rsa:2048 -keyout ~/logstash-5.1.2/pki/certs/logstash.key -out ~/logstash-5.1.2/pki/certs/logstash.crt
```

###1.3.2 Logstash的input

**filebeat.conf**

```
input {
    beats {
        port => 5044
        ssl => true
        ssl_certificate_authorities => ["/home/p4x/logstash-5.1.2/pki/certs/filebeat8.crt","/home/p4x/logstash-5.1.2/pki/certs/filebeat26.crt","/home/p4x/logstash-5.1.2/pki/certs/filebeat27.crt"]
        ssl_certificate => "/home/p4x/logstash-5.1.2/pki/certs/logstash.crt"
        ssl_key => "/home/p4x/logstash-5.1.2/pki/certs/logstash.key"
        ssl_verify_mode => "force_peer"
    }
}

filter {
    grok {
        match => {
            "message" => "(?<datetime>\d{4}-\d{2}-\d{2}\s\d{2}:\d{2}:\d{2},\d{3})\s-\s(?<type>(.*))\s+-\s(?<info>((tag:).*))"
        }
    }
}

output {
    if [info] =~ /^tag/ {
        stdout {
        }
    }
}
```

###1.3.3 FileBeat配置

**filebeat.yml**

```
#=========================== Filebeat prospectors =============================

filebeat.prospectors:

# Each - is a prospector. Most options can be set at the prospector level, so
# you can use different prospectors for various configurations.
# Below are the prospector specific configurations.

- input_type: log

  # Paths that should be crawled and fetched. Glob based paths.
  paths:
    #- /var/log/*.log
    - /home/p4x/ds-tomcat-8.5.8/logs/ff_server.log
    #- c:\programdata\elasticsearch\logs\*

#----------------------------- Logstash output --------------------------------
output.logstash:
  # The Logstash hosts
  hosts: ["host-10-18-16-28:5044"]

  # Optional SSL. By default is off.
  # List of root certificates for HTTPS server verifications
  ssl.certificate_authorities: ["/home/p4x/filebeat-5.2.1/pki/certs/logstash.crt"]

  # Certificate for SSL client authentication
  ssl.certificate: "/home/p4x/filebeat-5.2.1/pki/certs/filebeat.crt"

  # Client Certificate Key
  ssl.key: "/home/p4x/filebeat-5.2.1/pki/certs/filebeat.key"
```

##1.4 启动验证

```
# kafka
略

# logstash
./bin/logstash -f config/filebeat.conf

# filebeat
./filebeat -e -c filebeat.yml
```

#2. 收集日志到ES展示

流程：

Kafka => Logstash => ElasticSearch => Kibana

##2.1 Elasticsearch

**系统配置**

```
# 虚拟内存设置，追加：
vim /etc/sysctl.conf

vm.max_map_count=262144

# 修改文件句柄限制，追加
vim /etc/security/limits.conf

*    soft nofile 65536
*    hard nofile 65536
```

**ES配置**

```
cluster.name: p4x-app
node.name: p4x-node-1
network.host: 0.0.0.0
http.port: 9200
```

##2.2 Kibana

****

```
server.port: 5601
server.host: "192.168.1.2"
server.name: "p4x-kibana"
elasticsearch.url: "http://192.168.1.3:9200"
```

##2.3 Logstash

```
input {
    kafka {
        topics => ["p4xlogstash"]
        bootstrap_servers => "host-10-18-16-8:9092,host-10-18-16-28:9092,host-10-18-16-29:9092"
    }
}

filter {
    grok {
        match => {
            "message" => "(?<datetime>\d{4}-\d{2}-\d{2}\s\d{2}:\d{2}:\d{2},\d{3})\s-\s(?<level>\w+)\s+\[(?<threadname>(.*))]\s(?<clazz>(.*))\s\s-\s(?<info>(.*))"
        }
    }
    date {
        match => ["datetime", "yyyy-MM-dd HH:mm:ss,SSS"]
    }
    mutate {
        remove_field => ["datetime","threadname"]
        rename => ["info","message"]
    }
}

output {
    elasticsearch {
        hosts => ["10.18.16.3:9200"]
        index => "p4x"
    }
    stdout {
        codec => rubydebug
    }
}
```

## 启动验证

```
# ES
./bin/elasticsearch

# kafka
略

# Logstash
./bin/logstash -f config/elasticsearch.conf

# kibana
./bin/kibana
```