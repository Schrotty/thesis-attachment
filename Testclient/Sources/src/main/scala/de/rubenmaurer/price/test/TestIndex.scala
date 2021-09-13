package de.rubenmaurer.price.test

/**
 * Index with all available tests.
 */
object TestIndex {

  /**
   * Each test with its shortname.
   */
  private val _suites: Map[String, String] = Map(
    "ping" -> "ping.Ping",
    "pong" -> "ping.Pong",
    "unknown" -> "unknown.Unknown",
    "robustness" -> "robustness.Robustness",
    "whois" -> "whois.Whois",
    "basic-connection" -> "connection.BasicConnection",
    "multi-user-connection" -> "connection.MultiUserConnection",
    "quit-connection" -> "connection.QuitConnection",
    "private-message" -> "privmsg.NoticePrivateMessage",
    "motd" -> "motd.MessageOfTheDay",
    "who" -> "channel.WhoChannel",
    "join" -> "channel.JoinChannel",
    "topic" -> "channel.TopicChannel",
    "part" -> "channel.PartChannel",
    "assignment" -> "channel.AssignmentChannel",
    "list" -> "channel.ListChannel",
    "private-message-channel" -> "channel.PrivateMessageChannel"
  )

  /**
   * All suits with their tests.
   */
  private val _assignments: Map[String, List[String]] = Map(
    "ALL" -> _suites.values.toList,
    "CHANNEL" -> _suites.values.filter(x => x.contains("channel.")).toList,
    "CONNECTION" -> _suites.values.filter(x => x.contains("connection.")).toList,
    "MOTD" -> _suites.values.filter(x => x.contains("motd.")).toList,
    "PING" -> _suites.values.filter(x => x.contains("ping.")).toList,
    "PRIVMSG" -> _suites.values.filter(x => x.contains("privmsg.")).toList,
    "ROBUSTNESS" -> _suites.values.filter(x => x.contains("robustness.")).toList,
    "UNKNOWN" -> _suites.values.filter(x => x.contains("unknown.")).toList,
    "WHOIS" -> _suites.values.filter(x => x.contains("whois.")).toList
  )

  /**
   * Get all requested tests by its keys.
   *
   * @param keys The keys.
   * @return The tests.
   */
  def getAll(keys: String*): List[String] =
    getSuites(keys).appendedAll(getAssignments(keys))

  /**
   * Get all requested suites.
   *
   * @param suits The suites.
   * @return The tests.
   */
  def getSuites(suits: Seq[String]): List[String] =
    _suites.filter(suite => suits.contains(suite._1)).values.toList

  /**
   * Get all tests by its assigment.
   *
   * @param assignments The assignments.
   * @return The tests.
   */
  def getAssignments(assignments: Seq[String]): List[String] =
    _assignments.filter(as => assignments.contains(as._1)).values.flatten.toList
}
