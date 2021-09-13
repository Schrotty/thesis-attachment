package actors.mailbox

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed._

object MailboxPrinter {
  def main(args: Array[String]): Unit = {
    ActorSystem[String](Printer(), "mailbox-printer")
  }
}

private object Printer {
  def apply(): Behavior[String] = {
    Behaviors.setup[String] { context =>
      val limitedPrinter = context.spawn(Worker(context.self), "limited-worker", MailboxSelector.bounded(5))
      val unlimitedPrinter = context.spawn(Worker(context.self), "unlimited-worker")

      Range(0, 512).foreach(_ => {
        limitedPrinter ! ""
        unlimitedPrinter ! ""
      })

      context.stop(limitedPrinter)
      context.stop(unlimitedPrinter)

      Behaviors.receive[String] { (_, message) =>
        println(message)
        Behaviors.same
      }
    }
  }
}

private object Worker {
  def apply(actorRef: ActorRef[String]): Behavior[String] = {
    var messages: Int = 0

    Behaviors.receive[String] { (context, message) =>
      messages = messages + 1
      Behaviors.same
    }.receiveSignal {
      case (context, PostStop) =>
        actorRef ! s"[${context.self.path}] Received messages: $messages"
        Behaviors.same
    }
  }
}