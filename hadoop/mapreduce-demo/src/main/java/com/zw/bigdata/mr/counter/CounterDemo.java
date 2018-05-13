package com.zw.mr.counter;

import com.zw.util.HdfsUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import java.io.IOException;
import java.util.StringTokenizer;

/**
 * 计数器演示程序
 *
 * Created by zhangws on 16/10/11.
 */
public class CounterDemo {

    public static enum WORDS_IN_LINE_COUNTER {
        ZERO_WORDS,
        LESS_THAN_FIVE_WORDS,
        MORE_THAN_FIVE_WORDS
    }

    public static class CounterMapper extends Mapper<LongWritable, Text,
            Text, IntWritable> {

        private IntWritable countOfWords = new IntWritable(0);

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {

            StringTokenizer tokenizer = new StringTokenizer(value.toString());
            int words = tokenizer.countTokens();

            if (words == 0) {
                context.getCounter(WORDS_IN_LINE_COUNTER.ZERO_WORDS).increment(1);
            } else if (words > 0 && words <= 5) {
                context.getCounter(WORDS_IN_LINE_COUNTER.LESS_THAN_FIVE_WORDS).increment(1);
            } else {
                context.getCounter(WORDS_IN_LINE_COUNTER.MORE_THAN_FIVE_WORDS).increment(1);
            }
            while(tokenizer.hasMoreTokens()) {
                String target = tokenizer.nextToken();
                context.write(new Text(target), new IntWritable(1));
            }
        }
    }

    public static class CounterReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
        public void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {

            int count = 0;
            for (IntWritable v : values) {
                count += v.get();
            }
            //输出key
            context.write(key, new IntWritable(count));
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        String[] values = new GenericOptionsParser(conf, args).getRemainingArgs();
        if (values.length < 2) {
            System.err.println("Usage: wordcount <in> [<in>...] <out>");
            System.exit(2);
        }

        //先删除output目录
        HdfsUtil.rmr(conf, values[values.length - 1]);

        Job job = Job.getInstance(conf, CounterDemo.class.getSimpleName());

        job.setJarByClass(CounterDemo.class);

        job.setMapperClass(CounterMapper.class);
        job.setReducerClass(CounterReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(values[0]));
        FileOutputFormat.setOutputPath(job, new Path(values[1]));

        if (job.waitForCompletion(true)) {
            HdfsUtil.cat(conf, values[1] + "/part-r-00000");
            System.out.println("success");
        } else {
            System.out.println("fail");
        }
    }
}
