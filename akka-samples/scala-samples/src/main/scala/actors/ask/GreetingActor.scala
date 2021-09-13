package actors.ask

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object GreetingActor {
  final case class Greet(name: String, replyTo: ActorRef[String])

  def apply(): Behavior[Greet] =
    Behaviors.receive[Greet] {(context, message) =>
      message.replyTo ! s"Oh Hi ${message.name}!"
      Behaviors.stopped
    }
}
