package de.rubenmaurer.price.core.testing

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import de.rubenmaurer.price.core.testing.TestSuite.{SuiteFailure, SuiteSuccess}
import de.rubenmaurer.price.test.TestIndex
import de.rubenmaurer.price.util.Configuration

/**
 * Factory for generating a [[Behavior]] for an test-manger.
 */
object TestManager {

  /**
   * The base trait for commands.
   */
  sealed trait Command
  private final case class WrappedSuiteResponse(response: TestSuite.Response) extends Command

  /**
   * Creates a new [[Behavior]]
   *
   * @return The test-manager behavior.
   */
  def apply(): Behavior[Command] =
    Behaviors.setup { context =>
      val suiteMapper: ActorRef[TestSuite.Response] = context.messageAdapter(rsp => WrappedSuiteResponse(rsp))
      var testSuites: List[String] = TestIndex.getAll(Configuration.tests())

      def spawnTestSuite(): Behavior[Command] = {
          if (testSuites.isEmpty) return Behaviors.stopped
          context.spawnAnonymous(TestSuite(testSuites.head, suiteMapper)) ! TestSuite.Execute

          testSuites = testSuites.filter(s => !s.equals(testSuites.head))
          Behaviors.same
      }

      spawnTestSuite()
      Behaviors.receive[Command] { (_, message) =>
        message match {
          case wrapped: WrappedSuiteResponse =>
            wrapped.response match {
              case SuiteSuccess => spawnTestSuite()
              case failure: SuiteFailure =>
                context.log.error(failure.message.toString)
                Behaviors.stopped

              case _ => Behaviors.same
            }
        }
      }
    }
}

