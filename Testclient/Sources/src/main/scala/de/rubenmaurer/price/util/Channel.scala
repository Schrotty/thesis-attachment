package de.rubenmaurer.price.util

/**
 * A collection of pre-made channels.
 */
object Channel {
  def ALL: Channel = new Channel("*", true)
  def BLACKWELL: Channel = new Channel("blackwell")
  def BLACKWELL_ART: Channel = new Channel("blackwell-art")
  def BLACKWELL_SCIENCE: Channel = new Channel("blackwell-science")
  def DINER: Channel = new Channel("Blue-Whale-Diner")
}

/**
 * Represents a single irc-channel.
 *
 * @param name The name of the channel.
 * @param plain Flag for the channel name format.
 */
class Channel(val name: String, val plain: Boolean = false) extends Target {
  override def toString: String = if (plain) s"$name" else s"#$name"
}