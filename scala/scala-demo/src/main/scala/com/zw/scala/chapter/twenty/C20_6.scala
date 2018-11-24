package com.zw.scala.chapter.twenty

import scala.actors.Actor

case class Deposit(amount: Int)
case class Balance(bal: Double)

class AccountActor extends Actor {

    private var balance = 0.0

    override def act(): Unit = {
        while (true) {
            receive {
                case Deposit(amount) => {
                    balance += amount
                    // 这两种发送都可以
//                    sender ! Balance(balance)
                    reply(Balance(balance))
                }
            }
        }
    }
}

/**
  * Created by zhangws on 17/2/13.
  */
object C20_6 {
    def main(args: Array[String]) {
        val account: AccountActor = new AccountActor
        account.start()

        val reply = account !? Deposit(1000)
        reply match {
            case Balance(bal) => println("Current Balance: " + bal)
        }
    }
}


