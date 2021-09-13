package de.rubenmaurer.price.test

import com.typesafe.scalalogging.Logger

import java.util.concurrent.TimeUnit
import akka.util.Timeout
import de.rubenmaurer.price.core.facade._
import de.rubenmaurer.price.util.{Configuration, TemplateManager}
import org.scalactic.Requirements.requireNonNull
import org.scalatest._
import org.scalatest.events.{SeeStackDepthException, TestFailed}
import org.scalatest.funsuite.AnyFunSuite

import scala.collection.mutable.ListBuffer
import scala.concurrent.Await

/**
 * The parent class for all tests.
 * Starts and stops the session before each test.
 *
 * @param session The session to access in tests.
 * @param parser The parser to access in tests.
 * @param testName The name of the current test.
 */
class BaseTestSuite(session: Session, parser: Parser, testName: String) extends AnyFunSuite with BeforeAndAfter {
  before {
    if (!Await.result(session.start(testName), Timeout(3, TimeUnit.SECONDS).duration)) {
      Logger("test").info(TemplateManager.getConnectionFailure(Configuration.hostname()))
      fail()
    }
  }

  after {
    Logger("test").info("")

    session.stop()
  }

  /**
   * Runs a single test.
   * Keeps running even after a test failed.
   *
   * @param testName The name of the current test.
   * @param args Arguments for the execution of the current test.
   * @return The test status.
   */
  override def runTests(testName: Option[String], args: Args): Status = {
    requireNonNull(testName, args)

    import args._

    val theTestNames = testNames
    val statusBuffer = new ListBuffer[Status]()

    // If a testName is passed to run, just run that, else run the tests returned
    // by testNames.
    testName match {
      case Some(tn) =>
        val (filterTest, _) = filter(tn, tags, suiteId)
        if (!filterTest) {
          var status: Status = new StatefulStatus

          try {
            status = runTest(tn, args)
          } catch {
            case e: Throwable =>
              status = FailedStatus
              reporter(TestFailed(tracker.nextOrdinal(), e.getMessage, this.suiteName, this.suiteId, Some(this.getClass.getName), testName.get, "testText", null, null, Some(e), Some(50), Some(null), Option(SeeStackDepthException), this.rerunner, Option(null)))

          } finally {
            statusBuffer += status
          }
        }

      case None =>
        for ((tn, _) <- filter(theTestNames, tags, suiteId)) {
          if (!stopper.stopRequested) {
            statusBuffer += runTest(tn, args)
          }
        }
    }

    new CompositeStatus(Set.empty ++ statusBuffer)
  }
}
