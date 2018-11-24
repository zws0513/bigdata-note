package com.zw.kafka.storm;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 把输出保存到一个文件中
 * <p>
 *     把输出的消息放到文件kafkastorm.out中
 * </p>
 *
 * Created by zhangws on 16/10/2.
 */
public class SequenceBolt extends BaseBasicBolt {

    /**
     * Process the input tuple and optionally emit new tuples based on the input tuple.
     * <p>
     * All acking is managed for you. Throw a FailedException if you want to fail the tuple.
     *
     * @param input
     * @param collector
     */
    public void execute(Tuple input, BasicOutputCollector collector) {
        String word = (String) input.getValue(0);
        System.out.println("==============" + word);

        //写文件
        try {
            DataOutputStream out_file = new DataOutputStream(new FileOutputStream("/home/zkpk/kafkastorm.out"));
            out_file.writeUTF(word);
            out_file.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        collector.emit(new Values(word));
    }

    /**
     * Declare the output schema for all the streams of this topology.
     *
     * @param declarer this is used to declare output stream ids, output fields, and whether or not each output stream is a direct stream
     */
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("message"));
    }
}
