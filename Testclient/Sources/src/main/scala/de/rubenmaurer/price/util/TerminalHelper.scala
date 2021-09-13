package de.rubenmaurer.price.util

import org.apache.commons.lang.StringUtils
import org.fusesource.jansi.Ansi._

/**
 * Manages the terminal appearance.
 */
object TerminalHelper {
  val Success = "SUCCESS"
  val Pending = "PROCESSING"
  val FAILURE = "FAILURE"

  private val TerminalSize = 80

  /**
   * Display the startup message with version, build and runtime id.
   */
  def displayStartup(): Unit = {
    println(divider)
    println(centerAndWrap(TemplateManager.getStartupMessage))
    println(centerAndWrap(TemplateManager.getVersionString))
    println(centerAndWrap(TemplateManager.getRuntimeID))
    println(divider)
  }

  /**
   * Display the current test suite.
   *
   * @param suite The suite name.
   */
  def displayTestSuite(suite: String): Unit = {
    println(centerAndWrap("=== " + suite + " ==="))
    println(divider)
  }

  /**
   * Display the result of a test suite.
   */
  def displayTestSuiteResult(): Unit = {
    println(divider)
  }

  /**
   * Display the connection failure message.
   *
   * @param address The address to which the connection failed.
   */
  def displayConnectionFailure(address: String): Unit = {
    println(divider)
    println(centerAndWrap(TemplateManager.getConnectionFailure(address)))
    println(divider)
  }

  /**
   * Display the status of a test.
   *
   * @param test The test.
   * @param status The test status.
   * @param finish Has the test finished?
   */
  def displayTestStatus(test: String, status: String, finish: Boolean = false): Unit = {
    val emptySpace = " ".repeat(TerminalSize - (test.length + status.length) - 2)
    var finStatus = status

    status match {
      case Success => finStatus = ansi().fgGreen().render(status).fgDefault().toString
      case Pending => finStatus = ansi().fgBlue().render(status).fgDefault().toString
      case FAILURE => finStatus = ansi().fgRed().render(status).fgDefault().toString
    }

    printf("%s%s%s |%s", leftCage(test), emptySpace, finStatus, if (finish) "\r\n" else "\r")
  }

  private def divider: String = "+" + "-".repeat(TerminalSize) + "+"
  private def center(input: String): String = StringUtils.center(input, TerminalSize)
  private def centerAndWrap(input: String): String = "|" + center(input) + "|"

  private def leftCage(input: String): String = "| " + input
}
