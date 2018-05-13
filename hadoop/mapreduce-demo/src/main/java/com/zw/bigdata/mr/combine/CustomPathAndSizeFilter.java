package com.zw.bigdata.mr.combine;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 过滤器: 文件名需要匹配一个特定的正则表达式, 并满足最小文件大小.
 * <p>
 *     两个要求都有特定的作业参数:
 *     filter.name
 *     filter.min.size
 *     实现时需要扩展Configured类, 并实现PathFilter接口
 * </p>
 * <p>
 *     FileInputFormat.setInputPathFilter(job, CustomPathAndSizeFilter.class);
 * </p>
 *
 * Created by zhangws on 16/10/8.
 */
public class CustomPathAndSizeFilter extends Configured implements PathFilter {

    private Configuration configuration;
    private Pattern filePattern;
    private long filterSize;
    private FileSystem fileSystem;

    @Override
    public boolean accept(Path path) {
        boolean isFileAcceptable = true;
        try {
            if (fileSystem.isDirectory(path)) {
                return true;
            }
            if (filePattern != null) {
                Matcher m = filePattern.matcher(path.toString());
                isFileAcceptable = m.matches();
            }
            if (filterSize > 0) {
                long actualFileSize = fileSystem.getFileStatus(path).getLen();
                if (actualFileSize > this.filterSize) {
                    isFileAcceptable &= true;
                } else {
                    isFileAcceptable = false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isFileAcceptable;
    }

    @Override
    public void setConf(Configuration conf) {
        this.configuration = conf;
        if (this.configuration != null) {
            String filterRegex = this.configuration.get("filter.name");
            if (filterRegex != null) {
                this.filePattern = Pattern.compile(filterRegex);
            }

            String filterSizeString = this.configuration.get("filter.min.size");
            if (filterSizeString != null) {
                this.filterSize = Long.parseLong(filterSizeString);
            }
            try {
                this.fileSystem = FileSystem.get(this.configuration);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
