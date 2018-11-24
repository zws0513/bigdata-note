package com.zw.scala.chapter.twentyone

/**
  * Created by zhangws on 17/2/14.
  */
class Fraction() {

    private var n: Int = 0
    private var m: Int = 0

    def this(n: Int, m: Int) {
        this
        this.n = n
        this.m = m
    }

    def *(that: Fraction): Fraction = {
        Fraction(this.n * that.n, this.m * that.m)
    }

    override def toString() = {
        this.n + " " + this.m
    }
}

object Fraction {
    def apply(n: Int, m: Int) = {
        new Fraction(n, m)
    }

    // 定义隐式转换函数
    implicit def int2Fraction(n: Int) = Fraction(n, 1)
}

object C21_1 {

    def main(args: Array[String]) {
        // // 将调用int2Fraction，将整数3转换成一个Fraction对象。
        val result = 3 * Fraction(4, 5)
        println(result)
    }
}
