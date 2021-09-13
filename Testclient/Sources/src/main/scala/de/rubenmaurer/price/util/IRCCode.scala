package de.rubenmaurer.price.util

/**
 * A collection of all used irc-reply codes.
 */
object IRCCode extends Enumeration {
  type IRCCode = Int

  val none = 0
  val welcome = 1
  val your_host = 2
  val created = 3
  val my_info = 4
  val luser_client = 251
  val luser_op = 252
  val luser_unknown = 253
  val luser_channel = 254
  val luser_me = 255
  val who_is_user = 311
  val who_is_server = 312
  val end_of_who = 315
  val end_of_who_is = 318
  val list = 322
  val list_end = 323
  val no_topic = 331
  val topic = 332
  val who_reply = 352
  val name_reply = 353
  val end_of_names = 366
  val motd = 372
  val motd_start = 375
  val end_of_motd = 376
  val no_such_nick = 401
  val no_such_channel = 403
  val cannot_send_to_channel = 404
  val unknown_command = 421
  val no_motd = 422
  val nickname_in_use = 433
  val not_on_channel = 442
}
