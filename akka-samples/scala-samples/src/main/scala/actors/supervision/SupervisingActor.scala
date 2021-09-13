package actors.supervision

import akka.actor.typed.SupervisorStrategy
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}

import scala.concurrent.duration.DurationInt

object SupervisingActor {
  def main(args: Array[String]): Unit = {
    ActorSystem[String](Guardian(), "supervision")
  }
}

object Guardian {
  def apply(): Behavior[String] =
    Behaviors.supervise(behave())
      .onFailure[IllegalStateException](SupervisorStrategy.restart.withLimit(3, 3.seconds))

  def behave(): Behavior[String] =
    Behaviors.setup[String] { context =>
      println("Starting Actor...")
      throw new IllegalStateException("something went wrong")

      println("Actor started!") // unreachable
      Behaviors.stopped
    }
}