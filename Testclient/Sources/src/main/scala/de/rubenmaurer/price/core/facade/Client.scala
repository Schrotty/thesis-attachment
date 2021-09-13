package de.rubenmaurer.price.core.facade

import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter.{ClassicActorRefOps, TypedActorContextOps}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.util.Timeout
import de.rubenmaurer.price.PriceIRC
import de.rubenmaurer.price.core.facade.Client._
import de.rubenmaurer.price.core.facade.Session.facade.timeout
import de.rubenmaurer.price.core.networking.ConnectionHandler
import de.rubenmaurer.price.util.{Channel, Target, TemplateManager}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContextExecutor, TimeoutException}

/**
 * Factory for generating a [[Behavior]] for a [[Client]] which connects to an irc server.
 */
object Client {

  /**
   * Base trait for accepted messages.
   */
  sealed trait Command

  /**
   * A request for an actor.
   * It wraps a [[Command]] and an [[ActorRef]] of the actor which will receive a [[Response]].
   *
   * @param command The wrapped [[Command]].
   * @param replyTo The [[ActorRef]] of the receiving actor.
   */
  case class Request(command: Command, replyTo: ActorRef[Response]) extends Command

  /**
   * A response for a [[Request]].
   *
   * @param payload The response.
   */
  case class Response(payload: String) extends Command

  /**
   * The command to send a given message to the irc server.
   * Additional the amount of expected response lines is added.
   *
   * @param message The message to send.
   * @param expected The amount of expected response lines.
   */
  case class SendMessage(message: String, expected: Int) extends Command

  /**
   * The command for a [[Client]] to wait for a given amount of messages.
   *
   * @param messageCount The amount of messages the wait for.
   */
  case class Awaiting(messageCount: Int) extends Command

  /**
   * The command to disconnect from the irc server.
   */
  case class Disconnect() extends Command

  /**
   * A wrapper for the messages of a [[ConnectionHandler]] actor.
   *
   * @param response The wrapped message.
   */
  private case class WrappedConnectionResponse(response: ConnectionHandler.Response) extends Command

  /**
   * The Chloe preset for a client.
   */
  def CHLOE: Client = new Client("chloe", "elisabeth", "Chloe Elisabeth Price")

  /**
   * The Max preset for a client.
   */
  def MAX: Client = new Client("max", "maxine", "Maxine Caulfield")

  /**
   * The rachel preset for a client.
   */
  def RACHEl: Client = new Client("rachel", "ramber", "Rachel Amber")

  /**
   * The Kate preset for a client.
   */
  def KATE: Client = new Client("kate", "bunnymommy", "Kate Marsh")

  /**
   * Creates an new [[Behavior]].
   *
   * @param client The client which is linked to this actor/ behavior.
   * @return An actor behavior.
   */
  def apply(client: Client): Behavior[Command] = {
    Behaviors.setup { context =>
      context.log.debug(s"SPAWNED: ${client.username}")

      var response: List[String] = List()
      var expectedLines: Int = 0
      var tmpReplyTo: ActorRef[Response] = context.system.ignoreRef

      val responseMapper: ActorRef[ConnectionHandler.Response] = context.messageAdapter(rsp => WrappedConnectionResponse(rsp))
      val connectionHandler: ActorRef[ConnectionHandler.Request] = context.actorOf(ConnectionHandler.apply(responseMapper)).toTyped

      Behaviors.receive[Command] { (_, message) =>
        message match {
          case Request(command, replyTo) =>
            command match {
              case SendMessage(message, expected) =>
                response = List()
                expectedLines = expected
                tmpReplyTo = replyTo

                connectionHandler ! ConnectionHandler.Send(message, expected, responseMapper)
                Behaviors.same

              case Awaiting(messageCount) =>
                response = List()
                expectedLines = messageCount
                tmpReplyTo = replyTo

                Behaviors.same

              case Disconnect() =>
                connectionHandler ! ConnectionHandler.Disconnect
                Behaviors.same
            }

          case wrapped: WrappedConnectionResponse =>
            wrapped.response match {
              case ConnectionHandler.Received(payload) =>
                val rsp = payload.split("\r\n")
                response = rsp.toList ::: response

                if (response.length == expectedLines) tmpReplyTo ! Response(response.mkString("\r\n"))
                Behaviors.same

              case _ => Behaviors.same
            }

          case _ => Behaviors.same
        }
      }
    }
  }
}

/**
 * Representing a client which connects to a irc server.
 *
 * @param nickname The nickname of this client.
 * @param username The username of this client.
 * @param fullName The full name of this client.
 */
class Client(var nickname: String, val username: String, val fullName: String) extends Target {

  /**
   * The message log of a [[Client]].
   * It contains all received messages and provides methods to access them.
   */
  object log {

    /**
     * Contains all messages with its response code.
     */
    var codes: Map[Int, List[String]] = Map[Int, List[String]]()

    /**
     * Contains all messages without a response code.
     */
    var plain: List[String] = List[String]()

    /**
     * Returns the last received message with no response code.
     *
     * @return The last received message.
     */
    def last: String = plain.head

    /**
     * Looks for a message which starts with a given input and returns it.
     * If there is no matching entry, an empty string is returned.
     *
     * @param input The input to search for.
     * @return The matching message or an empty string.
     */
    def startWith(input: String): String = plain.find(_.startsWith(input)).getOrElse("")

    /**
     * Looks for a message which contains a given input and returns it.
     * If there is no entry containing the input, an error message is returned.
     *
     * @param input The input to search for.
     * @return The matching message or an error message.
     */
    def find(input: String): String = plain.find(_.contains(input)).getOrElse("<ERROR>")

    /**
     * Looks for a message which contains a given code and returns it.
     * If there is no matching message, an empty string is returned.
     *
     * @param code The code to seatch for.
     * @return The matching message or an empty string.
     */
    def byCode(code: Int): String = codes.getOrElse(code, List()).headOption.getOrElse("")

    /**
     * Looks for a message which contains a given code and a given text.
     * If there is no matching message, an empty string is returned.
     *
     * @param code The code to search for.
     * @param param The text to search for.
     * @return The matching message or an empty string.
     */
    def byCodeAnd(code: Int, param: String): String = codes.getOrElse(code, List()).find(s => s.contains(param)).getOrElse("")

    /**
     * Clears the log of plain messages.
     */
    def clearPlain(): Unit = plain = List()
  }

  private var _intern: ActorRef[Command] = _

  /**
   * Returns the [[ActorRef]] linked with this client.
   *
   * @return The [[ActorRef]].
   */
  def intern: ActorRef[Command] = _intern

  /**
   * Is this client linked with an actor?
   *
   * @return If this client is linked.
   */
  def linked: Boolean = intern != null

  /**
   * Link this client with an actor.
   *
   * @param actorRef The actor to link with.
   */
  def link(actorRef: ActorRef[Command]): Unit = {
    _intern = actorRef
  }

  /**
   * Sends a NICK and USER message for authentification to the irc server.
   *
   * @return The client.
   */
  def authenticate(): Client = {
    send(TemplateManager.getNick(this.nickname))
    send(TemplateManager.getUser(this.username, this.fullName), 10)
    this
  }

  /**
   * Send a USER and NICK message for authentification to the irc server.
   *
   * @return The client.
   */
  def authenticateWithSwapped(): Client = {
    send(TemplateManager.getUser(this.username, this.fullName))
    send(TemplateManager.getNick(this.nickname), 10)
    this
  }

  /**
   * Send a NICK and USER message for authentification to the irc server, but with additional whitespace.
   *
   * @return The client.
   */
  def authenticateWithWhitespace(): Client = {
    send(s"  ${TemplateManager.getNick(this.nickname)}  ")
    send(s"  ${TemplateManager.getUser(this.username, this.fullName)}  ", 10)
    this
  }

  /**
   * Send a NICK message with a nick which is already in use.
   *
   * @return The client.
   */
  def authenticateWithUsedNickname(): Client = {
    send(TemplateManager.getNick(this.nickname), 1)
    this
  }

  /**
   * Send a NICK message.
   *
   * @param nickname The nick.
   * @return The client.
   */
  def nick(nickname: String): Client = {
    send(TemplateManager.getNick(nickname), 1)
    this
  }

  /**
   * Send a NICK message.
   * If needed the client will wait for a amount of messages to arrive.
   *
   * @param expectResponse If the client should wait for messages.
   * @return The client.
   */
  def nick(expectResponse: Boolean = false): Client = {
    send(TemplateManager.getNick(this.nickname), if (expectResponse) 10 else 0)
    this
  }

  /**
   * Send a USER message.
   * If needed the client will wait for a amount of messages to arrive.
   *
   * @param expectResponse If the client should wait for messages.
   * @return The client.
   */
  def user(expectResponse: Boolean = false): Client = {
    send(TemplateManager.getUser(this.username, this.fullName), if (expectResponse) 10 else 0)
    this
  }

  /**
   * Send a JOIN message to join a given [[Channel]].
   *
   * @param channel The [[Channel]] to join.
   * @return The client.
   */
  def join(channel: Channel): Client = {
    send(TemplateManager.join(channel.name), 3)
    this
  }

  /**
   * Send a WHO message to identify a given channel.
   * After this the client waits for a given amount of messages.
   *
   * @param channel The channel.
   * @param amount The amount of messages to wait for.
   * @return The client.
   */
  def who(channel: String, amount: Integer): Client = {
    send(TemplateManager.who(channel), amount + 1)
    this
  }

  /**
   * Send a WHO message to identify a given channel.
   * After this the client waits for a given amount of messages.
   *
   * @param channel The channel.
   * @param amount The amount of messages to wait for.
   * @return The client.
   */
  def who(channel: Channel, amount: Integer = 1): Client = {
    send(TemplateManager.who(channel.toString), amount + 1)
    this
  }

  /**
   * Send a WHOIS for identifing a client.
   *
   * @param client The client to identify.
   * @param shouldFail
   * @return The client.
   */
  def whois(client: Client, shouldFail: Boolean = false): Client = {
    send(TemplateManager.whois(client.nickname), if(shouldFail) 1 else 3)
    this
  }

  /**
   * Send a QUIT message to disconnect from a server.
   *
   * @param message The QUIT message.
   * @return The client.
   */
  def quit(message: String): Client = {
    send(TemplateManager.getQuit(message), 1)
    disconnect()
    this
  }

  /**
   * Send a QUIT message to disconnect from a server.
   *
   * @return The client.
   */
  def quit(): Client = {
    send("QUIT", 1)
    this
  }

  /**
   * Send a PRIVMSG for a private conversation with a client.
   *
   * @param target The client to talk to.
   * @param message The message text.
   * @return The client.
   */
  def privateMessage(target: Client, message: String): Client = {
    send(TemplateManager.getPrivateMessage(target.nickname, message))
    this
  }

  /**
   * Send a PRIVMSG for a conversation with a channel.
   *
   * @param target The channel to talk to.
   * @param message The message text.
   * @return The client.
   */
  def privateMessage(target: Channel, message: String, expected: Integer = 0): Client = {
    send(TemplateManager.getPrivateMessage(target.toString, message), expected)
    this
  }

  /**
   * Send a NOTICE message for noticing a client.
   *
   * @param target The client.
   * @param message The notice text.
   * @return The client.
   */
  def notice(target: Client, message: String): Client = {
    send(TemplateManager.getNotice(target.nickname, message))
    this
  }

  /**
   * Send a NOTICE message for noticing a channel.
   *
   * @param target The channel.
   * @param message The notice text.
   * @return The client.
   */
  def notice(target: Channel, message: String): Client = {
    send(TemplateManager.getNotice(target.toString, message))
    this
  }

  /**
   * Send a MOTD message for receiving the message of the day.
   *
   * @return The client.
   */
  def motd(): Client = {
    send("MOTD", 3)
    this
  }

  /**
   * Send a LUSERS message.
   *
   * @return The client.
   */
  def lusers(): Client = {
    send("LUSERS", 5)
    this
  }

  /**
   * Send a TOPIC message to get the topic of a given channel.
   *
   * @param channel The channel.
   * @return The client.
   */
  def topic(channel: Channel): Client = {
    send(TemplateManager.getTopic(channel.toString), 1)
    this
  }

  /**
   * Send a TOPIC message to change the topic of a given channel.
   *
   * @param channel The channel.
   * @param topic The new topic.
   * @return The client.
   */
  def topic(channel: Channel, topic: String): Client = {
    send(TemplateManager.setTopic(channel.toString, topic), 1)
    this
  }

  /**
   * Send a PART message to leave a channel.
   *
   * @param channel The channel to leave.
   * @param message The leave message text.
   * @return The client.
   */
  def part(channel: Channel, message: String = null): Client = {
    send(TemplateManager.part(channel.toString, message), 1)
    this
  }

  /**
   * Send a LIST message.
   *
   * @param channel The channel.
   * @return The client.
   */
  def list(channel: Channel): Client = {
    send(TemplateManager.list(channel.toString), 2)
    this
  }

  /**
   * Wait for messages from another [[Client]].
   *
   * @param awaiting For what to wait.
   * @return The client.
   */
  def await(awaiting: () => Client): Client = {
    implicit val system: ActorSystem[_] = PriceIRC.system
    implicit val timeout: Timeout = 5.seconds

    Session.logger.info(s"$nickname EXEC -- Awaiting message(s)...")
    received(Await.result({
      val future = intern.ask[Response](replyTo => { Request(Awaiting(1), replyTo)})
      awaiting()

      future
    }, timeout.duration))
    this
  }

  /**
   * Send a message to the irc server.
   *
   * @param message The message to send.
   */
  def send(message: String): Unit = {
    implicit val system: ActorSystem[_] = PriceIRC.system
    implicit val ec: ExecutionContextExecutor = system.executionContext

    Session.logger.info(s"$nickname SEND -- $message")
    intern.ask[Response](replyTo => Request(SendMessage(message, 0), replyTo))
  }

  /**
   * Send a message to the irc server and wait for a response.
   *
   * @param message The message to send.
   * @param expected The amount of expected response lines.
   */
  def send(message: String, expected: Int = 0): Unit = {
    try {
      implicit val system: ActorSystem[_] = PriceIRC.system
      implicit val timeout: Timeout = 3.seconds
      implicit val ec: ExecutionContextExecutor = system.executionContext

      //log.clearPlain()
      Session.logger.info(s"$nickname SEND -- $message")
      received(
        Await.result(intern.ask[Response](replyTo => {
          Request(SendMessage(message, expected), replyTo)
        }), timeout.duration)
      )
    }
    catch
    {
      case e: TimeoutException => Session.logger.info(e.getMessage)
      case _: Throwable =>
    }
  }

  /**
   * Process received messages.
   *
   * @param response The received message.
   */
  private def received(response: Response): Unit = {
    val code = """(.+?)(\d{3})(.*)""".r
    for (line <- response.payload.split("\r\n")) {
      line match {
        case code(_, command, _) => log.codes = log.codes + (command.toInt -> (line :: log.codes.getOrElse(command.toInt, List())))
        case _ => log.plain = line :: log.plain
      }

      Session.logger.info(s"$nickname RECV -- $line")
    }
  }

  /**
   * Disconnect from the irc server.
   */
  def disconnect(): Unit = {
    implicit val system: ActorSystem[_] = PriceIRC.system
    implicit val ec: ExecutionContextExecutor = system.executionContext

    //Session.logger.info(s"$nickname SEND -- $message")
    intern.ask[Response](replyTo => Request(Disconnect(), replyTo))
  }

  /**
   * Create a new [[Client]] based on this client.
   *
   * @return The new client.
   */
  def copy: Client = {
    new Client(nickname, s"$username-copy", fullName)
  }
}