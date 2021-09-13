import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object Echo {
  sealed case class EchoMessage(message: String, replyTo: ActorRef[EchoMessage])

  def apply(): Behavior[EchoMessage] = {
    Behaviors.receive[EchoMessage] { (ctx, message) => {
      message.replyTo ! EchoMessage(message.message, ctx.system.ignoreRef)
      Behaviors.same
    }}
  }
}
