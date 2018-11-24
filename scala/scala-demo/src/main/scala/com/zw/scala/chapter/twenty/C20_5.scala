package com.zw.scala.chapter.twenty

import scala.actors.{!, Channel, Actor, OutputChannel}
import scala.actors.Actor._

case class Compute(input: Seq[Int], result: OutputChannel[Int])

class Computer extends Actor {
    override def act(): Unit = {
        while (true) {
            receive {
                case Compute(input, out) => { val answer = 3; out ! answer }
            }
        }
    }
}

/**
  * Created by zhangws on 17/2/13.
  */
object C20_5 {

    def main(args: Array[String]) {

        val computeActor: Computer = new Computer
        computeActor.start()

        val channel = new Channel[Int]
        val input: Seq[Int] = Seq(1, 2, 3)

        computeActor ! Compute(input, channel)

        // 匹配channel的receive
        channel.receive {
            case x => println(x)
        }

        actor {
            val computeActor2: Computer = new Computer
            computeActor2.start()

            val channel2 = new Channel[Int]
            val input2: Seq[Int] = Seq(1, 2, 3)

            computeActor2 ! Compute(input2, channel2)

            // 匹配actor自己的receive
            receive {
                case !(channel2, x) => println(x)
            }
        }
    }
}
