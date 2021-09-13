package de.rubenmaurer.price.util

import de.rubenmaurer.price.test.TestIndex
import org.stringtemplate.v4.{ST, STGroupFile}

/**
 * Manages the access of the string templates.
 */
object TemplateManager {
  val empty = ""
  private val _templates = new STGroupFile("templates/system.stg")

  private def getTemplate(key: String): String = {
    _templates.getInstanceOf(key).render()
  }

  private def getRawTemplate(key: String): ST = {
    _templates.getInstanceOf(key)
  }

  /**
   * Get the startup message.
   *
   * @return The startup message.
   */
  def getStartupMessage: String = getTemplate("startup")

  /**
   * Get the version string.
   *
   * @return The version string.
   */
  def getVersionString: String = getRawTemplate("version").add("v", BuildInfo.version).add("build", BuildInfo.buildInfoBuildNumber).render()

  /**
   * Get the runtime id string.
   *
   * @return The id string.
   */
  def getRuntimeID: String = getRawTemplate("runtime").add("id", Configuration.runtimeIdentifier).render()

  /**
   * Get all test names.
   *
   * @return The test names.
   */
  def getTests: String = getRawTemplate("tests").add("ts", TestIndex.getAll("ALL").mkString(",")).render()

  /**
   * Get the connection error message.
   *
   * @param address The address which couldn't connect to.
   * @return The error message.
   */
  def getConnectionFailure(address: String): String = getRawTemplate("connectionFailure").add("address", address).render()

  /**
   * Get the compare failure message.
   *
   * @param typ The compare type (string or integer)
   * @param actual The actual value.
   * @param line The line in which the error occurred.
   * @return The failure message.
   */
  def getCompareFailure(typ: String, actual: Any, line: String): String = getRawTemplate("compareFailure")
    .add("type", typ).add("actual", actual).add("line", line).render()

  /* === IRC REQUESTS ===*/

  /**
   * Get the nickname message.
   *
   * @param nickname The nickname to use.
   * @return The nickname message.
   */
  def getNick(nickname: String): String = getRawTemplate("nick").add("nickname", nickname).render()

  /**
   * Get the username message.
   *
   * @param username The username.
   * @param fullname The full name.
   * @return The username message.
   */
  def getUser(username: String, fullname: String): String = getRawTemplate("user").add("username", username)
    .add("fullname", fullname).render()

  /**
   * Get the whois message.
   *
   * @param nickname The nickname.
   * @return The whois message.
   */
  def whois(nickname: String): String = getRawTemplate("whois").add("nickname", nickname).render()

  /**
   * Get the quit message.
   *
   * @param message The quite message.
   * @return The quit message.
   */
  def getQuit(message: String): String = getRawTemplate("quit").add("message", message).render()

  /**
   * Get the private message text.
   *
   * @param nickname The targeted nickname.
   * @param message The message text.
   * @return The private message text.
   */
  def getPrivateMessage(nickname: String, message: String): String = getRawTemplate("privateMessage")
    .add("nickname", nickname).add("message", message).render()

  /**
   * Get the notice test.
   *
   * @param nickname The targeted nickname.
   * @param message The message text.
   * @return The notice text.
   */
  def getNotice(nickname: String, message: String): String = getRawTemplate("notice")
    .add("nickname", nickname).add("message", message).render()

  /**
   * Get the join message.
   *
   * @param channel The channel to join.
   * @return The join message.
   */
  def join(channel: String): String = getRawTemplate("join").add("channel", channel).render()

  /**
   * Get the who message.
   *
   * @param channel The channel to ask for.
   * @return The who message.
   */
  def who(channel: String): String = getRawTemplate("who").add("channel", channel).render()

  /**
   * Get the topic of a channel message.
   *
   * @param channel The channel.
   * @return The topic message.
   */
  def getTopic(channel: String): String = getRawTemplate("get_topic").add("channel", channel).render()

  /**
   * Get the 'set the topic' message.
   *
   * @param channel The channel.
   * @param topic The new topic.
   * @return The topic message.
   */
  def setTopic(channel: String, topic: String): String = getRawTemplate("set_topic").add("channel", channel)
    .add("topic", topic).render()

  /**
   * Get the part message.
   *
   * @param channel The channel to part.
   * @param message The part message.
   * @return The part message.
   */
  def part(channel: String, message: String): String = getRawTemplate("part").add("channel", channel).add("message", message).render()

  /**
   * Get the list message.
   *
   * @param channel The channel.
   * @return The list message.
   */
  def list(channel: String): String = getRawTemplate("list").add("channel", channel).render()
}
