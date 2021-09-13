package actors.request

import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}
import akka.actor.typed.scaladsl.Behaviors

object RequestResponse {
  def main(args: Array[String]): Unit = {
    ActorSystem[String](Guardian(), "request-response")
  }
}

private object Guardian {
  final case class Request(query: String, replyTo: ActorRef[Response])
  final case class Response(result: String)

  def requester(target: ActorRef[Request]): Behavior[Response] =
    Behaviors.setup[Response] { context =>
      target ! Request("PI", context.self)

      Behaviors.receive[Response] { (context, message) =>
        println(message.result)
        Behaviors.stopped
      }
    }

  def responder(): Behavior[Request] =
    Behaviors.receive[Request] { (context, message) =>
      println(s"Received actors.request: ${message.query}")

      message.replyTo ! Response(s"Your Result: ${Math.PI}")
      Behaviors.stopped
    }

  def apply(): Behavior[String] =
    Behaviors.setup[String] { context =>
      val responder: ActorRef[Request] = context.spawnAnonymous(Guardian.responder())
      val requester: ActorRef[Response] = context.spawnAnonymous(Guardian.requester(responder))
      context.watch(requester)

      Behaviors.receiveSignal {
        case (context, Terminated(ref)) =>
          Behaviors.stopped
      }
    }
}
