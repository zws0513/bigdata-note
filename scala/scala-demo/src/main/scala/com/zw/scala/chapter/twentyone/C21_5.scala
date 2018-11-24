package com.zw.scala.chapter.twentyone

// 以下两种引入都可以
//import com.zw.scala.chapter.twentyone.FrenchPunctuation._
import com.zw.scala.chapter.twentyone.FrenchPunctuation.quoteDelimiters

/**
  * Created by zhangws on 17/2/14.
  */
case class Delimiters(left: String, right: String)

object FrenchPunctuation {
    implicit val quoteDelimiters = Delimiters("<<", ">>")
}

object C21_5 {

    def quote(what: String)(implicit delims: Delimiters) =
        println(delims.left + what + delims.right)

    def main(args: Array[String]) {

        quote("Bonjour le monde")(Delimiters("<", ">"))

        // 将被隐式提供
        quote("Bonjour le monde")
    }
}
