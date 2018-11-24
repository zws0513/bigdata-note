package com.zw.bigdata.mr.join;

import com.zw.bigdata.hdfs.util.HdfsUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import java.io.IOException;

/**
 * Reduce侧连接演示示例
 * <p>
 * <pre>
 * <code>
 * hadoop jar mr-demo.jar \
 * com.zw.mr.join.ReduceJoinDemo \
 * /hw/hdfs/w1/province/province.txt \
 * /hw/hdfs/mr/w1/output/ms/part-r-00000 \
 * /hw/hdfs/mr/w1/output/rjd
 * </code>
 * </pre>
 * Created by zhangws on 16/10/13.
 */
public class ReduceJoinDemo {

    public static class ReduceJoinMapper extends Mapper<LongWritable, Text, Text, Text> {

        public void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {

            // 获取输入记录的字符串
            String line = value.toString();

            // 抛弃空记录
            if (line == null || line.equals("")) {
                return;
            }

            // 获取输入文件的全路径和名称
            FileSplit fileSplit = (FileSplit) context.getInputSplit();
            String path = fileSplit.getPath().toString();

            //处理来自tb_a表的记录
            if (path.contains("province.txt")) {
                context.write(new Text(line.substring(0, 2)), new Text("a#" + line));
            } else if (path.contains("part-r-00000")) {
                context.write(new Text(line.substring(0, 2)), new Text("b#"));
            }
        }
    }

    public static class ReduceJoinReducer extends Reducer<Text, Text, Text, NullWritable> {

        // province.txt存在, part-r-00000不存在的数据
        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            int count = 0;
            String province = "";
            for (Text value : values) {
                count++;
                String str = value.toString();
                if (str.startsWith("a#")) {
                    province = str.substring(2);
                }
            }

            if (count == 1) {
                context.write(new Text(province), NullWritable.get());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        if (otherArgs.length < 3) {
            System.err.println("Usage: <in> [<in>...] <out>");
            System.exit(2);
        }

        HdfsUtil.rmr(conf, otherArgs[otherArgs.length - 1]);

        Job job = Job.getInstance(conf, ReduceJoinDemo.class.getSimpleName());
        job.setJarByClass(ReduceJoinDemo.class);

        job.setMapperClass(ReduceJoinMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setReducerClass(ReduceJoinReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(NullWritable.class);

        FileInputFormat.setInputPaths(job, new Path(otherArgs[0]), new Path(otherArgs[1]));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[2]));

        if (job.waitForCompletion(true)) {
            HdfsUtil.cat(conf, otherArgs[2] + "/part-r-00000");
            System.out.println("success");
        } else {
            System.out.println("fail");
        }
    }
}
