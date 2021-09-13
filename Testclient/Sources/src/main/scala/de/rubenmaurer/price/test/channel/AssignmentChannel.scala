package de.rubenmaurer.price.test.channel

import de.rubenmaurer.price.core.facade.{Client, Parser, Session}
import de.rubenmaurer.price.test.BaseTestSuite
import de.rubenmaurer.price.util.Channel

class AssignmentChannel(session: Session, parser: Parser, testName: String) extends BaseTestSuite(session, parser, testName) {
  test("update nick") {
    val max: Client = session.spawnClient(Client.MAX).authenticate().nick("maxine")

    assert(parser.isNick(max))
  }

  test("quit without message") {
    val max: Client = session.spawnClient(Client.MAX).authenticate().join(Channel.DINER)

    assert(parser.isQuit(max.quit()))
  }

  test("quit with message") {
    val message: String = "Bye then!"
    val chloe: Client = session.spawnClient(Client.CHLOE).authenticate().join(Channel.DINER)

    assert(parser.isQuit(chloe.quit(message), message))
  }
}
