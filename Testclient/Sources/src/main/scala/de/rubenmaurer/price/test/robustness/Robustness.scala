package de.rubenmaurer.price.test.robustness

import de.rubenmaurer.price.core.facade.{Client, Parser, Session}
import de.rubenmaurer.price.test.BaseTestSuite

class Robustness(session: Session, parser: Parser, testName: String) extends BaseTestSuite(session, parser, testName) {
  test("sending only whitespace") {
    val max: Client = session.spawnClient(Client.MAX)
    max.send("     ")

    assert(parser.isEmpty(max))
  }

  test("sending auth. with whitespace") {
    val rachel: Client = session.spawnClient(Client.RACHEl).authenticateWithWhitespace()
    assert(parser.isWelcome(rachel))
  }
}
