package actors.watch

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior, Terminated}

import scala.util.Random

object WatchPrinter {
  def main(args: Array[String]): Unit = {
    ActorSystem[String](Printer(), "watch-printer")
  }
}

private object Printer {
  def worker(): Behavior[String] =
    Behaviors.setup[String] { context =>
      Thread.sleep(Random.between(2000, 10000))
      Behaviors.stopped
    }

  def apply(): Behavior[String] =
    Behaviors.setup[String] { context =>

      context.watch(context.spawnAnonymous(worker()))
      context.watchWith(context.spawnAnonymous(worker()), "Worker finished!")

      Behaviors.receive[String] { (_, message) =>
        println(message)

        Behaviors.same
      }.receiveSignal {
        case (_, Terminated(ref)) =>
          println(s"Actor '${ref.path}' terminated!")
          Behaviors.same
      }
    }
}
