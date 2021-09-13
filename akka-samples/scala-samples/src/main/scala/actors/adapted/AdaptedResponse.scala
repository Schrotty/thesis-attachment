package actors.adapted

import actors.adapted.Manager.{Calculate, Command}
import actors.adapted.Worker.{JobFinished, StartJob}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}

object AdaptedResponse {
  def main(args: Array[String]): Unit = {
    ActorSystem[String](guardian(), "request-response")
  }

  def guardian(): Behavior[String] =
    Behaviors.setup[String] { context =>
      val manager: ActorRef[Command] = context.spawn(Manager(), "job-manager")
      context.watch(manager)

      Range(1, 11).foreach(task => manager ! Calculate(task))

      Behaviors.receiveSignal {
        case (_, Terminated(_)) =>
          Behaviors.stopped
      }
    }
}

private object Manager {
  sealed trait Command
  final case class Calculate(task: Double) extends Command
  final case class MappedWorkerResponse(response: Worker.Response) extends Command

  def apply(): Behavior[Command] =
    Behaviors.setup[Command] { context =>
      var workerCounter: Int = 0
      val workerResponseMapper: ActorRef[Worker.Response] = context.messageAdapter(m => MappedWorkerResponse(m))

      Behaviors.receiveMessage[Command] {
        case Calculate(task) =>
          workerCounter = workerCounter + 1
          context.spawnAnonymous(Worker()) ! StartJob(task, workerResponseMapper)
          Behaviors.same

        case workerResponse: MappedWorkerResponse =>
          workerResponse.response match {
            case JobFinished(task, result) =>
              workerCounter = workerCounter - 1
              println(s"Job ($task) finished: $result")

              if (workerCounter == 0) Behaviors.stopped else Behaviors.same
          }
      }
    }
}

object Worker {
  sealed trait Request
  final case class StartJob(task: Double, replyTo: ActorRef[Response]) extends Request

  sealed trait Response
  final case class JobFinished(task: Double, result: Double) extends Response

  def apply(): Behavior[Request] =
    Behaviors.receiveMessage[Request] {
      case StartJob(task, replyTo) =>
        replyTo ! JobFinished(task, task*2)
        Behaviors.stopped
    }
}