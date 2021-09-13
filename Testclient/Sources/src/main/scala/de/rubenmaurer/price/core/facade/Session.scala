package de.rubenmaurer.price.core.facade

import java.util.concurrent.TimeUnit

import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter.{ClassicActorRefOps, TypedActorContextOps}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.io.Tcp.CommandFailed
import akka.util.Timeout
import com.typesafe.scalalogging.Logger
import de.rubenmaurer.price.PriceIRC
import de.rubenmaurer.price.core.facade.Session.{Request, SpawnClient, SpawnedClient}
import de.rubenmaurer.price.core.networking.ConnectionHandler
import de.rubenmaurer.price.util.Configuration
import de.rubenmaurer.price.util.Configuration.runtimeIdentifier
import org.slf4j.MDC

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.sys.process.{ProcessLogger, _}

/**
 * Factory for generating a [[Behavior]] for a [[Session]] for controlling an server.
 */
object Session {

  /**
   * Base trait for requests.
   */
  sealed trait Request

  /**
   * A request to spawn a new actor.
   *
   * @param client The client to spawn.
   * @param replyTo The [[ActorRef]] to link the [[Client]] with.
   */
  final case class SpawnClient(client: Client, replyTo: ActorRef[SpawnedClient]) extends Request

  /**
   * Request for the current server status.
   */
  final case object GetServerStatus extends Request

  /**
   * Request for the reporting the session status.
   */
  final case object ReportStatus extends Request

  /**
   * Base trait for responses.
   */
  sealed trait Response

  /**
   * Response when a [[Client]] was spawned.
   *
   * @param client The spawned [[Client]].
   */
  final case class SpawnedClient(client: Client) extends Response

  /**
   * Response when a connection exception occours.
   *
   * @param failureMessage The message why the connection failed.
   */
  final case class ConnectionError(failureMessage: CommandFailed) extends Response

  /**
   * Response when a test finished.
   */
  final case object TestFinished extends Response

  /**
   * A wrapper for the messages of a [[ConnectionHandler]] actor.
   *
   * @param response The wrapped message.
   */
  private case class WrappedConnectionResponse(response: ConnectionHandler.Response) extends Response with Request

  /**
   * The actors facade.
   */
  val facade: Session = new Session()

  /**
   * The used [[Logger]].
   */
  val logger: Logger = Logger("test")

  /**
   * Creates a new [[Behavior]].
   *
   * @param suite The test suite to execute.
   * @return An actor behavior.
   */
  def apply(suite: ActorRef[Response]): Behavior[Request] = {
    Behaviors.setup { context =>
      val connectionMapper: ActorRef[ConnectionHandler.Response] = context.messageAdapter(rsp => WrappedConnectionResponse(rsp))

      facade.intern = context.self
      Behaviors.receive[Request] { (context, message) =>
        message match {
          case GetServerStatus =>
            context.actorOf(ConnectionHandler.apply(connectionMapper)).toTyped
            Behaviors.same

          case SpawnClient(preset, replyTo) =>
            preset.link(context.spawn(Client(preset), preset.username))
            Thread.sleep(50)

            replyTo ! SpawnedClient(preset)
            Behaviors.same

          case ReportStatus =>
            context.children.foreach(f => context.stop(f))
            suite ! TestFinished
            Behaviors.same

          case wrapped: WrappedConnectionResponse =>
            wrapped.response match {
              case _: ConnectionHandler.Failure =>
                facade.online = false
                Behaviors.same

              case success: ConnectionHandler.Success =>
                facade.online = true
                success.replyTo ! ConnectionHandler.Disconnect
                Behaviors.same
            }
        }
      }
    }
  }
}

/**
 * Represents the current session.
 */
class Session() {
  implicit val timeout: Timeout = Timeout(3, TimeUnit.SECONDS)

  private var _process: Process = _
  private var _logLines: List[String] = List[String]()

  private var _intern: ActorRef[Request] = _
  private var _online: Boolean = false

  /**
   * Returns the [[ActorRef]] linked with this client.
   *
   * @return The [[ActorRef]].
   */
  def intern: ActorRef[Request] = _intern
  private def intern_= (ref: ActorRef[Request]): Unit = _intern = ref

  /**
   * Returns sessions status.
   *
   * @return The status.
   */
  def online: Boolean = _online
  private def online_= (status: Boolean): Unit = _online = status

  /**
   * Starts the session and the irc-server.
   *
   * @param testName The name of the test for this session.
   * @return The sessions status.
   */
  def start(testName: String): Future[Boolean] = {
    MDC.put("runtime-id", runtimeIdentifier.toString)
    MDC.put("test", testName.split('.').apply(1).capitalize)

    _online = false
    _logLines = _logLines.empty
    _process = Configuration.executable().run(ProcessLogger(line => _logLines = line :: _logLines , line => _logLines.appended(line)))

    Future {
      while (!online) {
        intern ! Session.GetServerStatus
        Thread.sleep(50)
      }

      online
    }
  }

  /**
   * Stops the session and the irc-server.
   */
  def stop(): Unit = {
    if (_process != null) {
      _process.destroy()

      Logger("process").info(_logLines.reverse.mkString("\r\n"))
      intern ! Session.ReportStatus

      if (_process.isAlive) {
        throw new Exception("ERROR_SHUTTING_DOWN_PROCESS")
      }
    }
  }

  /**
   * Spawns a new [[Client]] connected to an irc-server.
   *
   * @param client The client to spawn.
   * @return The connected client.
   */
  def spawnClient(client: Client): Client = {
    implicit val timeout: Timeout = Timeout(3, TimeUnit.SECONDS)
    implicit val system: ActorSystem[_] = PriceIRC.system

    Await.result(intern.ask[SpawnedClient](replyTo => SpawnClient(client, replyTo)), timeout.duration).client
  }
}
