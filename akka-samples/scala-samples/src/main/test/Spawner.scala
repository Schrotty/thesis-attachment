import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object Spawner {
  case class Spawn(sender: ActorRef[String])

  val child: Behaviors.Receive[Spawn] = {
      Behaviors.receive[Spawn]{ (_, message) =>
        message.sender ! "Spawned"
        Behaviors.same
      }
  }

  def apply(): Behavior[String] = {
    Behaviors.receive[String] { (ctx, message) =>
        message match {
          case "spawn" =>
            ctx.spawn(child, "welcome") ! Spawn(ctx.self)
            Behaviors.same

          case _ => Behaviors.stopped
        }
    }
  }
}
