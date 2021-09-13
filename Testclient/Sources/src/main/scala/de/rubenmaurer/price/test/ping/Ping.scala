package de.rubenmaurer.price.test.ping

import de.rubenmaurer.price.core.facade.{Client, Parser, Session}
import de.rubenmaurer.price.test.BaseTestSuite

class Ping(session: Session, parser: Parser, testName: String) extends BaseTestSuite(session, parser, testName) {
  test("sending ping") {
    val chloe: Client = session.spawnClient(Client.CHLOE).authenticate()
    chloe.send("PING", 1)

    assert(parser.isPong(chloe))
  }

  test("sending ping with multiple users") {
    val chloe: Client = session.spawnClient(Client.CHLOE).authenticate()
    val kate: Client = session.spawnClient(Client.KATE).authenticate()

    chloe.send("PING", 1)
    kate.send("PING", 1)

    assert(parser.isPong(chloe) && parser.isPong(kate))
  }
}