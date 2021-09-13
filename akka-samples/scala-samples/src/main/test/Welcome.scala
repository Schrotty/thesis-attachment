import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object Welcome {
  def apply(): Behavior[ActorRef[String]] = {
    Behaviors.receive[ActorRef[String]] { (_, to) =>
      to ! "Welcome!"
      Behaviors.same
    }
  }
}
