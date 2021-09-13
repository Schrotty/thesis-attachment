package actors.ask

import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.util.Timeout
import actors.ask.GreetingActor.Greet

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object AskFromOutside {
  var system: ActorSystem[String] = _

  def main(args: Array[String]): Unit = {
    system = ActorSystem[String](guardian(), "ask-from-outside")
  }

  def ask(actorRef: ActorRef[Greet]): Unit = {
    implicit val timeout: Timeout = 3.seconds
    implicit val system: ActorSystem[String] = AskFromOutside.system
    implicit val ec: ExecutionContextExecutor = system.executionContext

    val f: Future[String] = actorRef.ask[String](replyTo => Greet("Danny", replyTo))
    f.onComplete {
      case Success(value) => system ! value
      case Failure(exception) => system ! s"Something went wrong! ($exception)"
    }
  }

  def guardian(): Behavior[String] =
    Behaviors.setup[String] { context =>
      ask(context.spawn(GreetingActor(), "dummy-greeter"))

      Behaviors.receive[String] {(context, message) =>
        println(message)
        Behaviors.stopped
      }
    }
}
