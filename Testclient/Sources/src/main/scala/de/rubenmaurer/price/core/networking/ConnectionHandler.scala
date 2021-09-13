package de.rubenmaurer.price.core.networking

import java.net.InetSocketAddress

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.adapter.ClassicActorRefOps
import akka.actor.{Actor, Props}
import akka.io.Tcp.CommandFailed
import akka.io.{IO, Tcp}
import akka.util.ByteString
import de.rubenmaurer.price.core.networking.ConnectionHandler.{Disconnect, Response, Send}
import de.rubenmaurer.price.util.Configuration

/**
 * Factory for generating a connection actor.
 */
object ConnectionHandler {

  /**
   * Base trait for request.
   */
  sealed trait Request

  /**
   * A request for sending a message.
   *
   * @param message The message to send.
   * @param expected The expected amount of reply lines.
   * @param replyTo The to reply to.
   */
  final case class Send(message: String, expected: Int, replyTo: ActorRef[Response]) extends Request

  /**
   * Request for disconnecting.
   */
  final case object Disconnect extends Request

  /**
   * Base trait for a response.
   */
  sealed trait Response

  /**
   * Response for a received message.
   *
   * @param payload The received message.
   */
  final case class Received(payload: String) extends Response

  /**
   * Response for establishing a connection.
   *
   * @param replyTo The actor to reply to.
   */
  final case class Success(replyTo: ActorRef[Request]) extends Response

  /**
   * Response for a connection failure.
   *
   * @param failureMessage The error message.
   */
  final case class Failure(failureMessage: CommandFailed) extends Response

  /**
   * Creates a new connection actor.
   *
   * @param listener The actor this actor is listening for.
   * @return The properties for a new actor.
   */
  def apply(listener: ActorRef[Response]): Props = Props(new ConnectionHandler(listener))
}

/**
 * Classic actor which represents a tcp connection.
 *
 * @param listener The actor this actor is listening for.
 */
class ConnectionHandler(listener: ActorRef[Response]) extends Actor {

  import Tcp._
  import context.system

  //connect to server
  IO(Tcp) ! Connect(InetSocketAddress.createUnresolved(Configuration.hostname(), Configuration.port()))

  def receive: Receive = {
    case CommandFailed(c: Connect) =>
      listener ! ConnectionHandler.Failure(c.failureMessage)
      context.stop(self)

    case Connected(_, _) =>
      val connection = sender()
      connection ! Register(self)
      listener ! ConnectionHandler.Success(context.self.toTyped)

      context.become {
        case Send(payload, _, _) => connection ! Write(ByteString(payload.concat("\r\n")))
        case Received(data) => listener ! ConnectionHandler.Received(data.decodeString("US-ASCII"))
        case Disconnect => connection ! Close
        case _: ConnectionClosed => context.stop(self)
      }
  }
}
