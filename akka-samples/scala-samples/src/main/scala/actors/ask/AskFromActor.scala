package actors.ask

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.util.Timeout
import actors.ask.GreetingActor.Greet

import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

object AskFromActor {
  def main(args: Array[String]): Unit = {
    ActorSystem[String](guardian(), "ask-from-another-actor")
  }

  def guardian(): Behavior[String] =
    Behaviors.setup[String] { context =>
      implicit val timeout: Timeout = 3.seconds
      val g: ActorRef[Greet] = context.spawn(GreetingActor(), "dummy-greeter")

      context.ask[Greet, String](g, replyTo => Greet("Danny", replyTo)) {
        case Success(value) => value
        case Failure(_) => "Failed!"
      }

      Behaviors.receive[String] {(context, message) =>
        println(message)
        Behaviors.stopped
      }
    }
}
