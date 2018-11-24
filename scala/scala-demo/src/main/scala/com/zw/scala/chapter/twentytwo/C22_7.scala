package com.zw.scala.chapter.twentytwo

import java.io.File

import scala.util.continuations._

/**
  * Created by zhangws on 17/2/15.
  */
object C22_7 {

    var cont: (Unit => Unit) = null

    def processDirectory(dir: File): Unit@cps[Unit] = {
        val files = dir.listFiles
        var i = 0
        while (i < files.length) {
            val f = files(i)
            i += 1
            if (f.isDirectory) {
                processDirectory(f)
            } else {
                shift {
                    k: (Unit => Unit) => {
                        cont = k // 2
                    }
                } // 5
                println(f)
            }
        }
    }

    def main(args: Array[String]) {
        reset {
            processDirectory(new File("/Users/zhangws/Documents/doc/02.study/02.spark/scala/code/scala-demo")) // 1.
        } // 3
        for (i <- 1 to 100) cont() // 4
    }
}
