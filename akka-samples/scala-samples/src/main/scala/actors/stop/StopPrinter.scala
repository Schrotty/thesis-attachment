package actors.stop

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import actors.stop.PrinterManager.PrintMessage

object StopPrinter {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem[PrintMessage](PrinterManager(), "stop-printer")
    system ! PrintMessage("Hello there!")
  }
}

private object PrinterManager {
  final case class PrintMessage(text: String)

  def apply(): Behavior[PrintMessage] =
    Behaviors.receive[PrintMessage] { (context, message) =>
      val actorRef: ActorRef[String] = context.spawn(Printer(message.text), "printer-worker")
      context.stop(actorRef)

      Behaviors.stopped
    }
}

private object Printer {
  def apply(message: String): Behavior[String] =
    Behaviors.setup[String] { context =>
      println(message)
      Behaviors.same
    }
}