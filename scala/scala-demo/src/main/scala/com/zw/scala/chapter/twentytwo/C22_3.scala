package com.zw.scala.chapter.twentytwo

import scala.util.continuations._

/**
  * Created by zhangws on 17/2/15.
  */
object C22_3 {

    def main(args: Array[String]) {
        var cont: (Unit => Unit) = null
        reset {
            println("Before shift")
            shift {
                k: (Unit => Unit) => {
                    cont = k
                    println("Insert shift") // 跳转到reset末尾
                }
            }
            println("After shift")
        }
        println("After reset")
        cont()
    }
}
