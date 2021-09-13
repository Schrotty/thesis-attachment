package actors.simple

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import actors.simple.Printer.PrintMessage

object SimplePrinter {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem[PrintMessage](Printer(), "simple-printer")
    system ! PrintMessage("Hello there!")
  }
}

private object Printer {
  final case class PrintMessage(text: String)

  def apply(): Behavior[PrintMessage] =
    Behaviors.receive[PrintMessage] { (context, message) =>
      println(message.text)
      Behaviors.same
    }
}