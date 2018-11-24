package com.zw.scala.chapter.twenty

import scala.actors.Actor
import scala.actors.Actor._

case class Charge(creditCardNumber: Long, merchant: String, amount: Double)

class HiActor extends Actor {
    override def act(): Unit = {
        while (true) {
            receive {
                case "Hi" => println("Hello")
                case Charge(ccnum, merchant, amount) => println(ccnum + merchant + amount)
            }
        }
    }
}

/**
  * Created by zhangws on 17/2/13.
  */
object C20_1 {

    def main(args: Array[String]) {
        val actor1 = new HiActor
        actor1.start()

        actor1 ! "Hi"

        actor1 ! Charge(411111111, "样例类测试", 19.95)

        val actor2 = actor {
            while (true) {
                receive {
                    case "Hello" => println("world")
                    case Charge(ccnum, merchant, amount) => println(ccnum + merchant + amount)
                }
            }
        }

        actor2 ! "Hello"
    }
}
