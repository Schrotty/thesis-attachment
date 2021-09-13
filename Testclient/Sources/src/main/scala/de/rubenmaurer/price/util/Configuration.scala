package de.rubenmaurer.price.util

import com.typesafe.config.ConfigFactory

/**
 * Contains all application configurations.
 */
object Configuration {

  /**
   * The runtime id which is unique for every run. Used for naming the logging directory.
   */
  val runtimeIdentifier: Number = {
    val uid = java.util.UUID.randomUUID().hashCode()
    if (uid < 0) -uid else uid
  }

  private val configFactory = ConfigFactory.load()

  /**
   * Get the executable path.
   *
   * @return The path.
   */
  def executable(): String = configFactory.getString("price.executable")

  /**
   * Get the hostname of the server.
   *
   * @return The hostname.
   */
  def hostname(): String = configFactory.getString("price.hostname")

  /**
   * Get the port of the server.
   *
   * @return The port.
   */
  def port(): Int = configFactory.getInt("price.port")

  /**
   * Get the selected tests.
   *
   * @return The tests.
   */
  def tests():String = configFactory.getString("price.tests")

  /**
   * Get the logs path.
   *
   * @return The path.
   */
  def logs(): String = configFactory.getString("price.logs")

  /**
   * Is debug mode activated?
   *
   * @return Is debug mode?
   */
  def debug(): Boolean = configFactory.getBoolean("price.debug")
}
