package de.rubenmaurer.price

import akka.actor.typed.ActorSystem
import de.rubenmaurer.price.core.Guardian
import org.slf4j.LoggerFactory

/**
 * Central starting point for this application.
 */
object PriceIRC {

  /**
   * The actor system for this application.
   */
  var system: ActorSystem[_] = _

  /**
   * Main entry point.
   *
   * @param args program arguments
   */
  def main(args: Array[String]): Unit = {
    LoggerFactory.getLogger("system").info("Starting...")

    system = ActorSystem[Guardian.Command](Guardian(args), "guardian")
  }
}
