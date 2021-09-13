package actors.behavior

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import actors.behavior.Printer.PrintMessage

object BehaviorPrinter {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem[PrintMessage](Printer(), "behavior-printer")
    system ! PrintMessage("Hello there!")
    system ! PrintMessage("Oh Hi there!")
  }
}

private object Printer {
  final case class PrintMessage(text: String)

  def apply(): Behavior[PrintMessage] =
    Behaviors.receive[PrintMessage] { (context, message) =>
      println(message.text)

      Behaviors.receive[PrintMessage] { (context, message) =>
        println("Accepting no more messages!")
        Behaviors.stopped
      }
    }
}