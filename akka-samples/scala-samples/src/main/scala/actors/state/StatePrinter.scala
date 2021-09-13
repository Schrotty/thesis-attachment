package actors.state

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import actors.state.Printer.PrintMessage

object StatePrinter {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem[PrintMessage](Printer(), "state-printer")
    system ! PrintMessage("Hello there!")
    system ! PrintMessage("Oh, Hi there!")
    system ! PrintMessage("Oh, Hi Mark!")
  }
}

private object Printer {
  final case class PrintMessage(text: String)

  private val maxMessages: Int = 3
  private var messageCounter: Int = 0

  def apply(): Behavior[PrintMessage] =
    Behaviors.receive[PrintMessage] { (context, message) =>
      println(message.text)

      messageCounter = messageCounter + 1
      val messagesTillShutdown = maxMessages - messageCounter
      println(s"$messagesTillShutdown messages left before shutdown\r\n")

      if (messagesTillShutdown == 0) {
        println("Shutting down!")
        Behaviors.stopped
      }

      Behaviors.same
    }
}