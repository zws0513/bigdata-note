package com.zw.bigdata.mr.combine;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.CombineFileSplit;
import org.apache.hadoop.util.LineReader;

import java.io.IOException;

/**
 * 从CombineFileSplit中返回记录.
 * <p>
 *     CombineFileSplit与FileSplit之间的不同点在于是否存在包含多个偏移量和长度的多个路径.
 *
 *     自定义的RecordReader类会被分片中的每个文件调用,因此,自定义RecordReader类的构造函数
 *     必须有一个整型变量指明特定的文件正在用于产生记录.
 *
 *     第二个很重要的方法是nextKeyValue(),它负责产生下一个K-V对,getCurrentKey()与getCurrentValue()
 *     方法返回这个K-V对.
 * </p>
 *
 * Created by zhangws on 16/10/8.
 */
public class CustomCombineFileRecordReader extends RecordReader<LongWritable, Text> {

    private LongWritable key; // 当前位置在文件中的字节偏移量
    private Text value; // 当前所在行的文本
    private Path path;
    private FileSystem fileSystem;
    private LineReader lineReader; // 读取每一行数据
    private FSDataInputStream fsDataInputStream;
    private Configuration configuration;
    private int fileIndex;
    private CombineFileSplit combineFileSplit;
    private long start;
    private long end;

    public CustomCombineFileRecordReader(CombineFileSplit combineFileSplit,
                                         TaskAttemptContext taskAttemptContext,
                                         Integer index) throws IOException {
        this.fileIndex = index;
        this.combineFileSplit = combineFileSplit;
        this.configuration = taskAttemptContext.getConfiguration();
        this.path = combineFileSplit.getPath(index);
        this.fileSystem = this.path.getFileSystem(this.configuration);
        this.fsDataInputStream = fileSystem.open(this.path);
        this.lineReader = new LineReader(this.fsDataInputStream, this.configuration);
        this.start = combineFileSplit.getOffset(index);
        this.end = this.start + combineFileSplit.getLength(index);
        this.key = new LongWritable(0);
        this.value = new Text("");
    }

    @Override
    public void initialize(InputSplit split, TaskAttemptContext context)
            throws IOException, InterruptedException {

    }

    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException {
        int offset = 0;
        boolean isKeyValueAvailable = true;
        if (this.start < this.end) {
            offset = this.lineReader.readLine(this.value);
            this.key.set(this.start);
            this.start += offset;
        }

        if (offset == 0) {
            this.key.set(0);
            this.value.set("");
            isKeyValueAvailable = false;
        }
        return isKeyValueAvailable;
    }

    @Override
    public LongWritable getCurrentKey() throws IOException, InterruptedException {
        return key;
    }

    @Override
    public Text getCurrentValue() throws IOException, InterruptedException {
        return value;
    }

    @Override
    public float getProgress() throws IOException, InterruptedException {
        long splitStart = this.combineFileSplit.getOffset(fileIndex);
        if (this.start < this.end) {
            return Math.min(1.0f, (this.start - splitStart) /
                    (float) (this.end - splitStart));
        }
        return 0;
    }

    @Override
    public void close() throws IOException {
        if (lineReader != null) {
            lineReader.close();
        }
    }
}
