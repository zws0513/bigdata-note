package com.zw.scala.chapter.twentytwo

import java.awt.BorderLayout
import java.awt.event.{ActionEvent, ActionListener}
import javax.swing._

import scala.util.continuations._

/**
  * Created by zhangws on 17/2/15.
  */
object C22_8 extends App {

    val frame = new JFrame
    val button = new JButton("Next")
    setListener(button) {
        run()
    }
    val textField = new JTextArea(10, 40)
    val label = new JLabel("Welcome to the demo app")

    frame.add(label, BorderLayout.NORTH)
    frame.add(textField)

    val panel = new JPanel
    panel.add(button)
    frame.add(panel, BorderLayout.SOUTH)
    frame.pack()
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    frame.setVisible(true)

    def run(): Unit = {
        reset {
            val response1 = getResponse("What is your first name?")
            val response2 = getResponse("what is your last name?")
            process(response1, response2)
        }
    }

    def process(s1: String, s2: String): Unit = {
        label.setText("Hello, " + s1 + " " + s2)
    }

    var cont: Unit => Unit = null

    def getResponse(prompt: String): String@cps[Unit] = {
        label.setText(prompt)
        setListener(button) {
            cont()
        }
        shift {
            k: (Unit => Unit) => {
                cont = k
            }
        }
        setListener(button) {}
        textField.getText
    }

    def setListener(button: JButton)(action: => Unit): Unit = {
        for (l <- button.getActionListeners) button.removeActionListener(l)
        button.addActionListener(new ActionListener {
            override def actionPerformed(e: ActionEvent): Unit = {
                action
            }
        })
    }
}
