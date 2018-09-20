package com.zw.bigdata.spark.util

import java.io.OutputStream

import org.apache.hadoop.fs._
import org.apache.hadoop.io.IOUtils

/**
  * Created by zhangws on 2018/9/17.
  */
object HdfsUtil {

  /**
    * 向HDFS写入数据
    *
    * @param path hdfs文件（全路径）
    * @param data 待写入的数据
    * @param fs   FileSystem
    * @return 结果
    */
  def write(path: String, data: String, fs: FileSystem): Boolean = {
    try {
      val writer = fs.create(new Path(path), true)
      val str = data.getBytes("UTF-8")
      writer.write(str, 0, str.length)
      writer.close()
      fs.close()
      true
    }
    catch {
      case e: Exception =>
        println(e.getMessage)
        false
    }
  }

  /**
    * 重命名文件
    *
    * @param fromDir 元目录
    * @param toDir   目标目录
    * @param fs      FileSystem
    * @return 结果
    */
  def rename(fromDir: String, toDir: String, fs: FileSystem): Boolean = {
    try {
      fs.rename(new Path(fromDir), new Path(toDir))
    }
    catch {
      case e: Exception =>
        println(e.getMessage)
        false
    }
  }

  /**
    * 删除目录
    *
    * @param dir 被删除的目录
    * @param fs  FileSystem
    * @return 结果
    */
  def rmr(dir: String, fs: FileSystem): Boolean = {
    try {
      val targetPath = new Path(dir)
      fs.exists(targetPath) && fs.delete(targetPath, true)
    } catch {
      case e: Exception =>
        println(e.getMessage)
        false
    }
  }

  /**
    * 指定路径是否存在
    *
    * @param dir 指定路径
    * @param fs  FileSystem
    * @return 结果
    */
  def exists(dir: String, fs: FileSystem): Boolean = {
    try {
      fs.exists(new Path(dir))
    } catch {
      case e: Exception =>
        println(e.getMessage)
        false
    }
  }

  /**
    * 重命名目录
    *
    * @param fs      FileSystem``
    * @param fromDir 元目录
    * @param toDir   目标目录
    */
  def mvd(fs: FileSystem, fromDir: String, toDir: String): Boolean = {
    try {
      val from = new Path(fromDir)
      val to = new Path(toDir)
      fs.exists(from) && !fs.exists(to) && fs.rename(from, to)
    } catch {
      case e: Exception =>
        println(e.getMessage)
        false
    }
  }


  /**
    * 将文件复制到目录
    *
    * @param fs       FileSystem
    * @param fromPath 文件路径
    * @param toPath   目标文件路径
    */
  def copyFile(fs: FileSystem, fromPath: String, toPath: String): Unit = {
    val input = fs.open(new Path(fromPath))
    val output = fs.create(new Path(toPath))

    val b = new Array[Byte](1024)
    var hasRead = input.read(b)
    while (hasRead > 0) {
      output.write(b, 0, hasRead)
      hasRead = input.read(b)
    }
    input.close()
    output.close()
  }

  /**
    * 将目录复制到目录
    *
    * @param fs      FileSystem
    * @param fromDir 元目录
    * @param toDir   目标目录
    */
  def copyDir(fs: FileSystem, fromDir: String, toDir: String): Unit = {
    val srcPath = new Path(fromDir)
    val strs = fromDir.split("/")
    val lastName = strs(strs.length - 1)
    if (fs.isDirectory(srcPath)) {
      fs.mkdirs(new Path(toDir + "/" + lastName))
      //遍历
      val fileStatus = fs.listStatus(srcPath)
      for (fileSta <- fileStatus) {
        copyDir(fs, fileSta.getPath.toString, toDir + "/" + lastName)
      }
    }
    else {
      fs.mkdirs(new Path(toDir))
      System.out.println("src" + fromDir + "\n" + toDir + "/" + lastName)
      copyFile(fs, fromDir, toDir + "/" + lastName)
    }
  }

  /**
    * 将临时目录中的结果文件mv到源目录
    * <p>
    * 如果未指定<code>fileName</code>，则以ahr-为文件前缀
    * </p>
    *
    * @param fs      FileSystem
    * @param fromDir 元目录
    * @param toDir   目标目录
    */
  def mvdir(fs: FileSystem, fromDir: String, toDir: String): Unit = {
    fs.rename(new Path(fromDir), new Path(toDir))
  }

  /**
    * 将指定目录下的所有子目录移到目标目录
    *
    * @param fs      FileSystem
    * @param fromDir 元目录
    * @param toDir   目标目录
    */
  def mvdir2(fs: FileSystem, fromDir: String, toDir: String): Unit = {
    val srcFiles: Array[Path] = FileUtil.stat2Paths(fs.listStatus(new Path(fromDir)))
    for (p: Path <- srcFiles) {
      fs.rename(p, new Path(toDir))
    }
  }

  /**
    * 将临时目录中的结果文件mv到源目录
    * <p>
    * 如果未指定<code>fileName</code>，则以ahr-为文件前缀
    * </p>
    *
    * @param fs       FileSystem
    * @param fromDir  元目录
    * @param toDir    目标目录
    * @param fileName 目标文件名
    */
  def mv(fs: FileSystem, fromDir: String, toDir: String, fileName: String = null): Unit = {
    val srcFiles: Array[Path] = FileUtil.stat2Paths(fs.listStatus(new Path(fromDir)))
    for (p: Path <- srcFiles) {
      // 如果是以part开头的文件则修改名称
      if (p.getName.startsWith("part")) {
        if (fileName != null && !fileName.isEmpty) {
          fs.rename(p, new Path(toDir + "/" + fileName))
        } else {
          fs.rename(p, new Path(toDir + "/ahr-" + p.getName))
        }
      }
    }
  }

  /**
    * 将临时目录中的结果文件mv到源目录
    * <p>
    * 如果未指定<code>fileName</code>，则以ahr-为文件前缀
    * 如果有同名文件，这覆盖
    * </p>
    *
    * @param fs       FileSystem
    * @param fromDir  元目录
    * @param toDir    目标目录
    * @param fileName 目标文件名
    */
  def mvf(fs: FileSystem, fromDir: String, toDir: String, fileName: String = null): Unit = {
    val srcFiles: Array[Path] = FileUtil.stat2Paths(fs.listStatus(new Path(fromDir)))
    for (p: Path <- srcFiles) {
      // 如果是以part开头的文件则修改名称
      if (p.getName.startsWith("part")) {
        if (fileName != null && !fileName.isEmpty) {
          val toFile = new Path(toDir + "/" + fileName)
          if (fs.exists(toFile)) fs.delete(toFile, false)
          fs.rename(p, toFile)
        } else {
          fs.rename(p, new Path(toDir + "/ahr-" + p.getName))
        }
      }
    }
  }

  /**
    * 删除指定目录中的文件
    *
    * @param fs     FileSystem
    * @param dir    指定目录
    * @param filter 文件过滤器
    */
  def del(fs: FileSystem, dir: String, filter: PathFilter): Unit = {
    val path = new Path(dir)
    if (fs.exists(path)) {
      val files: Array[Path] = FileUtil.stat2Paths(fs.listStatus(path, filter))
      for (f: Path <- files) {
        fs.delete(f, true) // 迭代删除文件或目录
      }
    }
  }

  /**
    * 删除指定目录中的所有文件
    *
    * @param fs  FileSystem
    * @param dir 指定目录
    */
  def delDir(fs: FileSystem, dir: String): Unit = {
    val path = new Path(dir)
    if (fs.exists(path)) {
      fs.delete(path, true) // 迭代删除文件或目
    }
  }

  /**
    * 删除指定目录中的所有文件
    *
    * @param fs  FileSystem
    * @param dir 指定目录
    */
  def del(fs: FileSystem, dir: String): Unit = {
    val path = new Path(dir)
    if (fs.exists(path)) {
      val files: Array[Path] = FileUtil.stat2Paths(fs.listStatus(path))
      for (f: Path <- files) {
        fs.delete(f, true) // 迭代删除文件或目录
      }
    }
  }

  /**
    * 合并文件
    *
    * @param fs      FileSystem
    * @param srcDir  元目录
    * @param dstFile 目标文件
    */
  def merge(fs: FileSystem, srcDir: String, dstFile: String): Unit = {
    val src = new Path(srcDir)
    if (fs.getFileStatus(src).isDirectory) {
      val out: OutputStream = fs.create(new Path(dstFile))

      try {
        val contents: Array[FileStatus] = fs.listStatus(src)
        for (c <- contents if c.isFile) {
          val in = fs.open(c.getPath)
          try {
            IOUtils.copyBytes(in, out, fs.getConf, false)
          } finally in.close()
        }
      } finally out.close()
    }
  }

  /**
    * 创建目录
    *
    * @param fs         FileSystem
    * @param remotePath 远端目录
    */
  def mkdir(fs: FileSystem, remotePath: String): Unit = {
    val path = new Path(remotePath)
    if (!fs.exists(path)) {
      fs.mkdirs(path)
    }
  }
}

/**
  * 操作HDFS文件的过滤器
  */
class DefaultFileFilter(val fileName: String) extends PathFilter {

  /**
    * <p>
    * 如果<code>fileName</code>指定，则该文件不处理；
    * 否则，"ahr-"开头的文件不处理。
    * </p>
    *
    * @param path 文件路径
    * @return
    */
  @Override def accept(path: Path): Boolean = {
    if (fileName != null && !fileName.isEmpty) {
      !path.getName.startsWith(fileName)
    } else {
      !path.getName.startsWith("ahr-")
    }
  }
}
