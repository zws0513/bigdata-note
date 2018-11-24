package com.zw.bigdata.mr.combine;

import com.zw.bigdata.hdfs.util.HdfsUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import java.io.IOException;

/**
 * 测试类
 *
 * Created by zhangws on 16/10/8.
 */
public class CustomCombineFilesDemo {

    public static class CombineFilesMapper extends Mapper<LongWritable, Text, LongWritable, Text> {

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            context.write(key, value);
        }
    }

    public static void main(String[] args)
            throws IOException, InterruptedException, ClassNotFoundException {

        GenericOptionsParser parser = new GenericOptionsParser(args);
        Configuration config = parser.getConfiguration();
        String[] remainingArgs = parser.getRemainingArgs();

        //先删除output目录
        HdfsUtil.rmr(config, remainingArgs[remainingArgs.length - 1]);

        // 实例化任务
        Job job = Job.getInstance(config, CustomCombineFilesDemo.class.getSimpleName());
        // 设置任务类
        job.setJarByClass(CustomCombineFilesDemo.class);

        // 设置Map任务类
        job.setMapperClass(CombineFilesMapper.class);
        // 设置Reduce类(此处设置为没有Reduce任务)
        job.setNumReduceTasks(0);

        // 设置输入格式
        job.setInputFormatClass(CustomCombineFileInputFormat.class);
        // 设置输入过滤器
        FileInputFormat.setInputPathFilter(job, CustomPathAndSizeFilter.class);
        // 设置输入文件或路径
        FileInputFormat.addInputPath(job, new Path(remainingArgs[0]));

        // 设置输出格式
        job.setOutputFormatClass(TextOutputFormat.class);
        // 设置输出目录
        TextOutputFormat.setOutputPath(job, new Path(remainingArgs[1]));
        // 设置输出类型
        job.setOutputKeyClass(LongWritable.class);
        job.setOutputValueClass(Text.class);

        // 启动并等待任务完成
        job.waitForCompletion(true);
    }
}
