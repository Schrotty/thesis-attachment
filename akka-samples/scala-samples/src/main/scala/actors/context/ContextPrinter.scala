package actors.context

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import actors.context.Printer.PrintMessage

import java.util.Calendar

object ContextPrinter {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem[PrintMessage](Printer(), "simple-printer")
    system ! PrintMessage("Hello there!")
  }
}

private object Printer {
  final case class PrintMessage(text: String)

  def date(): Behavior[PrintMessage] = Behaviors.setup { context =>
    println(Calendar.getInstance().getTime)
    Behaviors.stopped
  }

  def apply(): Behavior[PrintMessage] = {
    Behaviors.setup { context =>
      val reference: ActorRef[PrintMessage] = context.spawn(date(), "datetime-actor")

      Behaviors.receive[PrintMessage] { (context, message) =>
        println(message.text)
        context.log.info(s"Received message: ${message.text}")

        Behaviors.same
      }
    }
  }
}