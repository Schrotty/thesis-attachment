package de.rubenmaurer.price.test.connection

import de.rubenmaurer.price.core.facade.{Client, Parser, Session}
import de.rubenmaurer.price.test.BaseTestSuite

class QuitConnection(session: Session, parser: Parser, testName: String) extends BaseTestSuite(session, parser, testName) {
  test("quit with message") {
    val message = "Bye"
    val client: Client = session.spawnClient(Client.RACHEl).authenticate().quit(message)

    assert(parser.isQuit(client, message))
  }

  test("plain quit") {
    assert(parser.isQuit(session.spawnClient(Client.KATE).authenticate().quit()))
  }

  test("multi-user quit") {
    val max: Client = session.spawnClient(Client.MAX).authenticate().quit("Goodbye!")
    val chloe: Client = session.spawnClient(Client.CHLOE).authenticate().quit("See ya!")

    assert(parser.isQuit(max, "Goodbye!"))
    assert(parser.isQuit(chloe, "See ya!"))
  }
}
