package actors.future

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object ReceiveFuture {
  def main(args: Array[String]): Unit = {
    ActorSystem[Int](guardian(), "receive-future")
  }

  def compute(): Future[Int] = {
    Future {
      Thread.sleep(3 * 1000)
      42
    }
  }

  def guardian(): Behavior[Int] =
    Behaviors.setup[Int] { context =>
      println("Starting...")

      context.pipeToSelf(compute()) {
        case Success(value) => value
        case Failure(_) => -1
      }

      Behaviors.receive[Int] { (_, message) =>
        println(message)
        Behaviors.stopped
      }
    }
}
