package com.zw.bigdata.hdfs.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 操作HDFS工具类
 * <p>
 * Created by zhangws on 16/8/4.
 */
public class HdfsUtil {

    /**
     * 删除指定目录
     *
     * @param conf    HDFS配置
     * @param dirPath 待删除目录
     *
     * @throws IOException
     */
    public static boolean rmr(Configuration conf, String dirPath) throws IOException {
        boolean delResult = false;
//        FileSystem fs = FileSystem.get(conf);
        Path targetPath = new Path(dirPath);
        FileSystem fileSystem = targetPath.getFileSystem(conf);
        if (fileSystem.exists(targetPath)) {
            delResult = fileSystem.delete(targetPath, true);
            if (delResult) {
                System.out.println(targetPath + " has been deleted sucessfullly.");
            } else {
                System.out.println(targetPath + " deletion failed.");
            }
        }
        return delResult;
    }

    /**
     * 获取指定目录下的文件列表
     * <p/>
     * <p>
     * 返回值中是绝对路径, 路径之间以逗号分隔
     * </p>
     *
     * @param conf    HDFS配置
     * @param dirPath 指定目录
     *
     * @return 文件列表, 格式: "文件A全路径,文件B全路径"
     *
     * @throws IOException
     */
    public static String ls(Configuration conf, String dirPath) throws IOException {
        String fileNames = "";

        // 遍历目录下的所有文件
        FileSystem fs = FileSystem.get(conf);
        FileStatus[] status = fs.listStatus(new Path(dirPath));
        for (FileStatus file : status) {
            if (file.isFile()) {
                fileNames = fileNames + file.getPath() + ",";
            }
        }
        return fileNames.length() > 0 ? fileNames.substring(0, fileNames.length() - 1) : fileNames;
    }

    /**
     * 输出指定文件内容
     *
     * @param conf     HDFS配置
     * @param filePath 文件路径
     *
     * @throws IOException
     */
    public static void cat(Configuration conf, String filePath) throws IOException {

//        FileSystem fileSystem = FileSystem.get(conf);
        InputStream in = null;
        Path file = new Path(filePath);
        FileSystem fileSystem = file.getFileSystem(conf);
        try {
            in = fileSystem.open(file);
            IOUtils.copyBytes(in, System.out, 4096, true);
        } finally {
            if (in != null) {
                IOUtils.closeStream(in);
            }
        }
    }

    /**
     * 读取HDFS文件内容
     *
     * @param conf     HDFS配置
     * @param filePath 文件路径
     *
     * @return 文件内容
     *
     * @throws IOException
     */
    public static String read(Configuration conf, String filePath) throws IOException {
        InputStream in = null;
        Path file = new Path(filePath);
        FileSystem fileSystem = file.getFileSystem(conf);
        try {
            in = fileSystem.open(file);
            BufferedInputStream bis = new BufferedInputStream(in);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            int c;
            while ((c = bis.read()) != -1) {
                baos.write(c);
            }
            return baos.toString();
        } finally {
            if (in != null) {
                IOUtils.closeStream(in);
            }
        }
    }

    /**
     * 从HDFS中下载指定文件到本地
     *
     * @param conf              HDFS配置
     * @param remoteSrcFilePath HDFS文件绝对路径
     * @param localDstFilePath  本地文件绝对路径
     *
     * @return 是否下载成功
     *
     * @throws IOException
     */
    public static void get(Configuration conf, String remoteSrcFilePath,
                           String localDstFilePath) throws IOException {

        FileSystem fs = FileSystem.get(conf);
        fs.copyToLocalFile(new Path(remoteSrcFilePath), new Path(localDstFilePath));
    }

    /**
     * 上传本地文件到HDFS
     *
     * @param conf              HDFS配置
     * @param localSrcFilePath  本地文件全路径
     * @param remoteDstFilePath HDFS文件全路径
     *
     * @return 是否上传成功
     *
     * @throws IOException
     */
    public static void put(Configuration conf, String localSrcFilePath,
                           String remoteDstFilePath) throws IOException {

        FileSystem fs = FileSystem.get(conf);
        fs.copyFromLocalFile(new Path(localSrcFilePath), new Path(remoteDstFilePath));
    }

    /**
     * 创建目录
     * <p>
     * 调用示例:
     * <code>mkdir(new Configuration(), "hdfs://master:9000/w2/utf/input");</code>
     * </p>
     *
     * @param conf       HDFS配置
     * @param remotePath 远端目录
     *
     * @throws IOException
     */
    public static void mkdir(Configuration conf, String remotePath) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        fs.mkdirs(new Path(remotePath));
    }

    /**
     * 修改指定目录的权限
     *
     * @param conf    HDFS配置
     * @param dirPath 目录
     * @param mod     权限
     *
     * @return 是否修改成功
     *
     * @throws IOException
     */
    public static void chmod(Configuration conf, String dirPath, short mod) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        fs.setPermission(new Path(dirPath), new FsPermission(mod));
    }
}
