package de.rubenmaurer.price.core

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import de.rubenmaurer.price.core.testing.TestManager
import de.rubenmaurer.price.util.TerminalHelper

/**
 * The guardian actor and entry point for all other actors.
 */
object Guardian {

  /**
   * The base trait for commands.
   */
  sealed trait Command

  /**
   * The command for a finished suite.
   */
  private final case object SuiteFinished extends Command

  /**
   * Generates a new [[Behavior]].
   *
   * @param args The command line arguments.
   * @return The behavior.
   */
  def apply(args: Array[String]): Behavior[Command] =
    Behaviors.setup { context =>

      // spawn & watch children
      context.watchWith(context.spawn(TestManager(), "test-manager"), SuiteFinished)

      // print startup message
      TerminalHelper.displayStartup()

      Behaviors.receive[Command] { (_, message) =>
        message match {
          case SuiteFinished => Behaviors.stopped
        }
      }
    }
}