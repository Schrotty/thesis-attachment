package de.rubenmaurer.price.core.testing

import de.rubenmaurer.price.util.TerminalHelper
import org.scalatest.Reporter
import org.scalatest.events._

/**
 * Custom ScalaTest reporter for displaying the current test status.
 *
 * @param testCount The amout of tests.
 */
class PriceReporter(testCount: Integer) extends Reporter {
  private val _currentSuite: String = ""
  private var _testCount = testCount

  /**
   * Depending on the event the status of a test is updated.
   *
   * @param event The event.
   */
  def apply(event: Event): Unit = {
    event match {
      case TestStarting(ordinal, suiteName, _, suiteClassName, testName, _, _, _, _, _, _, _) =>
        if (_currentSuite != suiteName) this.apply(SuiteStarting(ordinal, suiteName, suiteName, suiteClassName))
        TerminalHelper.displayTestStatus(testName, TerminalHelper.Pending)

      case TestSucceeded(ordinal, suiteName, _, suiteClassName, testName, _, _, _, _, _, _, _, _, _) =>
        _testCount = _testCount - 1
        TerminalHelper.displayTestStatus(testName, TerminalHelper.Success, finish = true)

        if (_testCount == 0) this.apply(SuiteCompleted(ordinal, suiteName, suiteName, suiteClassName))

      case TestFailed(_, _, _, _, _, testName, _, _, _, _, _, _, _, _, _, _, _) =>
        _testCount = _testCount - 1
        TerminalHelper.displayTestStatus(testName, TerminalHelper.FAILURE, finish = true)

      case _ =>
    }
  }
}
