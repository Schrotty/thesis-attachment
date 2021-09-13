package de.rubenmaurer.price.test.ping

import de.rubenmaurer.price.core.facade.{Client, Parser, Session}
import de.rubenmaurer.price.test.BaseTestSuite

class Pong(session: Session, parser: Parser, testName: String) extends BaseTestSuite(session, parser, testName) {
  test("sending a pong") {
    val chloe: Client = session.spawnClient(Client.CHLOE)
    chloe.send("PONG")

    assert(parser.isEmpty(chloe))
  }
}
