package com.zw.bigdata.mr.combine;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.CombineFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.CombineFileRecordReader;
import org.apache.hadoop.mapreduce.lib.input.CombineFileSplit;

import java.io.IOException;

/**
 * 合并文件,唯一需要重写的是createRecordReader()方法.
 * <p>
 *     在getSplits()方法中返回一个CombineFileSplit分片对象.每个分片可能合并了来自不同文件的不同块.
 *     如果使用setMaxSplitSize()方法设置了分片的最大容量,本地节点的文件将会合并到一个分片,超出分片最大
 *     容量的部分将与同一机架的其他主机的块合并.
 *     如果没有设置这个最大容量,合并只会在同一机架内进行.
 * </p>
 *
 * Created by zhangws on 16/10/8.
 */
public class CustomCombineFileInputFormat extends CombineFileInputFormat<LongWritable, Text> {

    @Override
    public RecordReader<LongWritable, Text> createRecordReader(InputSplit split,
                                                               TaskAttemptContext context)
            throws IOException {

        return new CombineFileRecordReader<>((CombineFileSplit) split,
                context, CustomCombineFileRecordReader.class);
    }
}
