package com.zw.bigdata.mr.combine;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

import java.io.InputStream;
import java.net.URI;

/**
 * Created by zhangws on 16/10/9.
 */
public class SequenceFileDemo {
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();

        FileSystem fs = FileSystem.get(new URI("hdfs://hsm01:9000"), conf);

        //输入路径：文件夹
        FileStatus[] files = fs.listStatus(new Path(args[0]));

        Text key = new Text();
        Text value = new Text();

        //输出路径：文件
        SequenceFile.Writer writer = SequenceFile.createWriter(fs, conf, new Path(args[1]), key.getClass(), value.getClass());
        InputStream in = null;
        byte[] buffer = null;

        for (FileStatus file : files) {
            key.set(file.getPath().getName());
            in = fs.open(file.getPath());
            buffer = new byte[(int) file.getLen()];
            IOUtils.readFully(in, buffer, 0, buffer.length);
            value.set(buffer);
            IOUtils.closeStream(in);
            System.out.println(key.toString() + "\n" + value.toString());
            writer.append(key, value);
        }

        IOUtils.closeStream(writer);
    }
}
