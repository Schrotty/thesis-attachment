package de.rubenmaurer.price.core.facade

import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.util.Timeout
import de.rubenmaurer.price.PriceIRC
import de.rubenmaurer.price.antlr4.{IRCLexer, IRCParser}
import de.rubenmaurer.price.core.facade.Parser.{Parse, ParseData, ParseResult}
import de.rubenmaurer.price.core.parser.antlr.{PricefieldErrorListener, PricefieldListener}
import de.rubenmaurer.price.util.{Channel, IRCCode, Target}
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}

import java.util.concurrent.TimeUnit
import scala.concurrent.Await

/**
 * Factory for generating a [[Behavior]] for a [[Parser]] to validate incoming messages.
 */
object Parser {

  /**
   * The actors facade.
   */
  val facade: Parser = new Parser()

  /**
   * The base trait for accepted messages.
   */
  sealed trait Command

  /**
   * A request for parsing a given message and validating its content.
   *
   * @param message The message to parse.
   * @param parserRule The rule to use for parsing.
   * @param replyTo The actor to reply to.
   * @param data Data used for the validating.
   */
  final case class Parse(message: String, parserRule: Int, replyTo: ActorRef[ParseResult], data: ParseData) extends Command

  /**
   * The response for a finished parse process.
   *
   * @param errors A list of occurred error.
   */
  final case class ParseResult(errors: List[String]) extends Command

  /**
   * Data needed for the validating of a messages content.
   *
   * @param code The irc-command code.
   * @param user A clients username.
   * @param nick A clients nickname.
   * @param fullname A clients full name.
   * @param message A message e.g. from a private message.
   * @param users The amount of users.
   * @param channels The amount of channels.
   * @param unknown The amount of unknown clients.
   * @param clients The amount of clients.
   * @param command The command text.
   * @param channel The name of a channel.
   * @param names A sequence of names.
   * @param target The target of a message.
   */
  final case class ParseData(code: Int = 0, user: String = "", nick: String = "", fullname: String = "",
                             message: String = "", users: Int = 0, channels: Int = 0, unknown: Int = 0, clients: Int = 0,
                             command: String = "", channel: String = "", names: Seq[String] = List(), target: String = "")

  /**
   * Creates a new [[Behavior]].
   *
   * @return An actor behavior.
   */
  def apply(): Behavior[Parse] = Behaviors.setup { context =>
    facade.intern = context.self

    Behaviors.receive[Parse] { (_, request) =>
      val parserListener = new PricefieldListener(request.data)
      val errorListener = new PricefieldErrorListener()
      val lexer = new IRCLexer(CharStreams.fromString(request.message))

      val parser = new IRCParser(new CommonTokenStream(lexer))
      parser.removeErrorListeners()
      parser.addErrorListener(errorListener)

      val rule = request.parserRule match {
        case IRCParser.RULE_pong => parser.pong()
        case IRCParser.RULE_unknown_command => parser.unknown_command()
        case IRCParser.RULE_motd => parser.motd()

        /* === WELCOME === */
        case IRCParser.RULE_welcome => parser.welcome()
        case IRCParser.RULE_your_host => parser.your_host()
        case IRCParser.RULE_created => parser.created()
        case IRCParser.RULE_my_info => parser.my_info()

        /* === WHOIS === */
        case IRCParser.RULE_who_is_user => parser.who_is_user()
        case IRCParser.RULE_who_is_server => parser.who_is_server()
        case IRCParser.RULE_end_of_who_is => parser.end_of_who_is()

        /* === PRIVATE MESSAGES/ NOTICE === */
        case IRCParser.RULE_private_message => parser.private_message()
        case IRCParser.RULE_notice => parser.notice()

        /* === LUSER === */
        case IRCParser.RULE_luser_client => parser.luser_client()
        case IRCParser.RULE_luser_op => parser.luser_op()
        case IRCParser.RULE_luser_unknown => parser.luser_unknown()
        case IRCParser.RULE_luser_channel => parser.luser_channel()
        case IRCParser.RULE_luser_me => parser.luser_me()

        /* === JOIN === */
        case IRCParser.RULE_name_reply => parser.name_reply()
        case IRCParser.RULE_end_of_names => parser.end_of_names()

        /* === WHO === */
        case IRCParser.RULE_who => parser.who()
        case IRCParser.RULE_end_of_who => parser.end_of_who()

        /* === UTIL ===*/
        case IRCParser.RULE_no_such_nick_channel => parser.no_such_nick_channel()
        case IRCParser.RULE_nickname_in_use => parser.nickname_in_use()
        case IRCParser.RULE_quit => parser.quit()
        case IRCParser.RULE_topic => parser.topic()

        case _ => parser.response()
      }

      parser.removeParseListeners()
      rule.enterRule(parserListener)

      request.replyTo ! ParseResult(parserListener.errors ++ errorListener.exceptions)
      Behaviors.same
    }
  }
}

/**
 * Represents the parser.
 */
class Parser() {
  private var _intern: ActorRef[Parse] = _
  private implicit val timeout: Timeout = Timeout(3, TimeUnit.SECONDS)
  private implicit val system: ActorSystem[_] = PriceIRC.system

  /**
   * Returns the [[ActorRef]] linked with this client.
   *
   * @return The [[ActorRef]].
   */
  def intern: ActorRef[Parse] = _intern
  private def intern_= (ref: ActorRef[Parse]): Unit = _intern = ref

  /**
   * Parses a message.
   *
   * @param message The message to parse.
   * @param parserRule The parser rule to use fpr parsing.
   * @param parseData The data used for validating.
   * @return The parse result.
   */
  private def parse(message: String, parserRule: Int, parseData: ParseData): ParseResult = {
    if (message.isBlank || message.isEmpty) {
      throw new RuntimeException(s"Empty parse messages are not allowed!")
    }

    Await.result(intern.ask[ParseResult](replyTo => Parse(message, parserRule, replyTo, parseData)), timeout.duration)
  }

  private def isValid(message: String, parserRule: Int, parseData: ParseData = ParseData()): Boolean = {
    try {
      val result = parse(message, parserRule, parseData)
      if (result.errors.nonEmpty) {
        throw new RuntimeException(result.errors.head)
      }
    } catch {
      case e: Throwable =>
        Session.logger.error(e.getMessage)
        return false
    }

    true
  }

  /* === PLAIN LOG METHODS === */

  /**
   * Is the clients plain log empty?
   *
   * @param client The client.
   * @return Is log empty?
   */
  def isEmpty(client: Client): Boolean = {
    client.log.plain.isEmpty
  }

  /* === NON CODE METHODS */

  /**
   * Is message a valid pong message?
   *
   * @param client The client.
   * @return Is valid?
   */
  def isPong(client: Client): Boolean = {
    isValid(client.log.startWith("PONG"), IRCParser.RULE_pong)
  }

  /**
   * Is message a valid nick message?
   *
   * @param client The client.
   * @return Is valid?
   */
  def isNick(client: Client): Boolean = {
    isValid(client.log.find("NICK"), IRCParser.RULE_nick_reply, ParseData(nick = client.nickname))
  }

  /**
   * Is message a valid quit message?
   *
   * @param client The client.
   * @param quitMessage The quit message.
   * @return Is valid?
   */
  def isQuit(client: Client, quitMessage: String = "Client Quit"): Boolean = {
    isValid(client.log.last, IRCParser.RULE_quit, ParseData(message = quitMessage))
  }

  /**
   * Is message a valid private message?
   *
   * @param client The client.
   * @param message The private message text.
   * @return Is valid?
   */
  def isPrivateMessage(client: Client, message: String): Boolean = {
    isValid(client.log.last, IRCParser.RULE_private_message, ParseData(message = message))
  }

  /**
   * Is message a valid notice message?
   *
   * @param client The client.
   * @param message The notice message.
   * @return Is valid?
   */
  def isNotice(client: Client, message: String): Boolean = {
    isValid(client.log.last, IRCParser.RULE_notice, ParseData(message = message))
  }

  /**
   * Is message a valid part message?
   *
   * @param client The client.
   * @param channel The channel the client parts.
   * @param message The part message text.
   * @return Is valid?
   */
  def isPart(client: Client, channel: Channel, message: String = ""): Boolean = {
    isValid(client.log.find("PART"), IRCParser.RULE_part, ParseData(channel = channel.toString, message = message))
  }

  /* === CODE LOG METHODS === */

  /**
   * Is message a valid unknown message?
   *
   * @param client The client.
   * @param command The command which is unknown.
   * @return Is valid?
   */
  def isUnknown(client: Client, command: String): Boolean = {
    isValid(client.log.byCode(IRCCode.unknown_command), IRCParser.RULE_unknown_command, ParseData(command = command))
  }

  /**
   * Is message a valid welcome message?
   *
   * @param client The client.
   * @return Is valid?
   */
  def isWelcome(client: Client): Boolean = {
    isValid(client.log.byCode(IRCCode.welcome), IRCParser.RULE_welcome, ParseData(nick = client.nickname, user = client.username)) &&
      isValid(client.log.byCode(IRCCode.your_host), IRCParser.RULE_your_host) &&
      isValid(client.log.byCode(IRCCode.created), IRCParser.RULE_created) &&
      isValid(client.log.byCode(IRCCode.my_info), IRCParser.RULE_my_info)
  }

  /**
   * Is message a valid message of the day?
   *
   * @param client The client.
   * @param message The message of the day.
   * @return Is valid?
   */
  def isMessageOfTheDay(client: Client, message: String): Boolean = {
    isValid(client.log.byCode(IRCCode.motd_start), IRCParser.RULE_motd_start) &&
      isValid(client.log.byCode(IRCCode.motd), IRCParser.RULE_motd, ParseData(message = message)) &&
      isValid(client.log.byCode(IRCCode.end_of_motd), IRCParser.RULE_end_of_motd)
  }

  /**
   * Is message a no message of the day message?
   *
   * @param client The client.
   * @return Is valid?
   */
  def isNoMessageOfTheDay(client: Client): Boolean = {
    isValid(client.log.byCode(IRCCode.no_motd), IRCParser.RULE_no_motd)
  }

  /**
   * Is message a valid who is message?
   *
   * @param client The client.
   * @param target The target of the who is.
   * @return Is valid?
   */
  def isWhois(client: Client, target: Client): Boolean = {
    isValid(client.log.byCode(IRCCode.who_is_user), IRCParser.RULE_who_is_user, ParseData(nick = target.nickname, user = target.username, fullname = target.fullName)) &&
      isValid(client.log.byCode(IRCCode.who_is_server), IRCParser.RULE_who_is_server, ParseData(nick = target.nickname)) &&
      isValid(client.log.byCode(IRCCode.end_of_who_is), IRCParser.RULE_end_of_who_is, ParseData(nick = target.nickname))
  }

  /**
   * Is message a valid there is no such nick?
   *
   * @param client The client.
   * @param target The unknown nick.
   * @return Is valid?
   */
  def isNoSuchNick(client: Client, target: Client): Boolean = {
    isValid(client.log.byCode(IRCCode.no_such_nick), IRCParser.RULE_no_such_nick_channel, ParseData(target = target.nickname))
  }

  /**
   * Is valid nickname is in use?
   *
   * @param client The client.
   * @return Is valid?
   */
  def isNicknameInUse(client: Client): Boolean = {
    isValid(client.log.byCode(IRCCode.nickname_in_use), IRCParser.RULE_nickname_in_use)
  }

  /**
   * Is valid luser message?
   *
   * @param client The client.
   * @param clients The amount of clients.
   * @param channels The amount channels.
   * @param unknown The amount of unknown connections.
   * @param users The amount of users.
   * @return Is valid?
   */
  def isLUser(client: Client, clients: Int = 1, channels: Int = 0, unknown: Int = 0, users: Int = 1): Boolean = {
    isValid(client.log.byCode(IRCCode.luser_client), IRCParser.RULE_luser_client, ParseData(clients = clients)) &&
      isValid(client.log.byCode(IRCCode.luser_op), IRCParser.RULE_luser_op) &&
      isValid(client.log.byCode(IRCCode.luser_unknown), IRCParser.RULE_luser_unknown, ParseData(unknown = unknown)) &&
      isValid(client.log.byCode(IRCCode.luser_channel), IRCParser.RULE_luser_channel, ParseData(channels = channels)) &&
      isValid(client.log.byCode(IRCCode.luser_me), IRCParser.RULE_luser_me, ParseData(users = users))
  }

  /**
   * Is message a valid join message?
   *
   * @param client The client.
   * @param channel The channel to join.
   * @param names The joined clients.
   * @return Is valid?
   */
  def isJoin(client: Client, channel: Channel, names: Client*): Boolean = {
    isValid(client.log.byCode(IRCCode.name_reply), IRCParser.RULE_name_reply, ParseData(channel = channel.toString, names = names.map(f => f.nickname))) &&
      isValid(client.log.byCode(IRCCode.end_of_names), IRCParser.RULE_end_of_names, ParseData(channel = channel.toString))
  }

  /**
   * Is message a valid who message?
   *
   * @param client The client.
   * @param channel The channel.
   * @param user The user.
   * @return Is valid?
   */
  def isWho(client: Client, channel: Channel, user: Client*): Boolean = {
    user.forall(f => isValid(client.log.byCodeAnd(IRCCode.who_reply, f.fullName), IRCParser.RULE_who, ParseData(channel = channel.toString, nick = f.nickname, user = f.username, fullname = f.fullName))) &&
      isValid(client.log.byCode(IRCCode.end_of_who), IRCParser.RULE_end_of_who, ParseData(channel = channel.toString))
  }

  /**
   * Is a valid topic message?
   *
   * @param client The client.
   * @param channel The channel which topic is changed.
   * @param message The new topic.
   * @return is valid?
   */
  def isTopic(client: Client, channel: Channel, message: String = ""): Boolean = {
    isValid(client.log.byCode(IRCCode.topic), IRCParser.RULE_topic, ParseData(channel = channel.toString, message = message)) ||
      isValid(client.log.find("TOPIC"), IRCParser.RULE_topic, ParseData(channel = channel.toString, message = message))
  }

  /**
   * Is message a valid no topic message?
   *
   * @param client The client.
   * @param channel The channel.
   * @return Is valid?
   */
  def isNoTopicSet(client: Client, channel: Channel): Boolean = {
    isValid(client.log.byCode(IRCCode.no_topic), IRCParser.RULE_no_topic, ParseData(channel = channel.toString))
  }

  /**
   * Is valid no on channel message?
   *
   * @param client The client.
   * @param channel The channel.
   * @return Is valid?
   */
  def isNotOnChannel(client: Client, channel: Channel): Boolean = {
    isValid(client.log.byCode(IRCCode.not_on_channel), IRCParser.RULE_not_on_channel, ParseData(channel = channel.toString))
  }

  /**
   * Is valid no such channel message?
   *
   * @param client The client.
   * @param target The not existing target.
   * @return Is valid?
   */
  def isNoSuchChannel(client: Client, target: Target): Boolean = {
    isValid(client.log.byCode(IRCCode.no_such_channel), IRCParser.RULE_no_such_nick_channel, ParseData(target = target.toString)) ||
      isValid(client.log.byCode(IRCCode.no_such_nick), IRCParser.RULE_no_such_nick_channel, ParseData(target = target.toString))
  }

  /**
   * Is message a valid list message?
   *
   * @param client The client.
   * @param channel The channel.
   * @param clients The amount of clients.
   * @param topic The channels topic.
   * @return Is valid.
   */
  def isList(client: Client, channel: Channel, clients: Int = 1, topic: String = ""): Boolean = {
    isValid(client.log.byCode(IRCCode.list), IRCParser.RULE_list, ParseData(channel = channel.toString, clients = clients, message = topic)) &&
      isValid(client.log.byCode(IRCCode.list_end), IRCParser.RULE_listend)
  }

  /**
   * Is message a valid cannot send to channel message?
   *
   * @param client The client.
   * @param channel The channel.
   * @return Is valid?
   */
  def isCannotSendToChannel(client: Client, channel: Channel): Boolean = {
    isValid(client.log.byCode(IRCCode.cannot_send_to_channel), IRCParser.RULE_cannot_send_to_channel, ParseData(channel = channel.toString))
  }
}
