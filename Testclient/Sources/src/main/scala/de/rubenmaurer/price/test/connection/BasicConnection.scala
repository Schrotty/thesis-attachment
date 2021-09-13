package de.rubenmaurer.price.test.connection

import de.rubenmaurer.price.core.facade.{Client, Parser, Session}
import de.rubenmaurer.price.test.BaseTestSuite

class BasicConnection(session: Session, parser: Parser, testName: String) extends BaseTestSuite(session, parser, testName) {
  test("simple auth.") {
    assert(parser.isWelcome(session.spawnClient(Client.KATE).authenticate()))
  }

  test("simple auth. swapped nick/user") {
    assert(parser.isWelcome(session.spawnClient(Client.CHLOE).authenticateWithSwapped()))
  }

  test("multi user. one full auth.") {
    session.spawnClient(Client.CHLOE).nick()
    session.spawnClient(Client.RACHEl).nick()

    val kate: Client = session.spawnClient(Client.KATE).nick().user(true)
    assert(parser.isWelcome(kate))
  }

  test("multi user. one full auth. with swapped nick") {
    session.spawnClient(Client.CHLOE).nick()
    session.spawnClient(Client.RACHEl).nick()

    val kate: Client = session.spawnClient(Client.KATE).user().nick(true)
    assert(parser.isWelcome(kate))
  }

  test("no unexpected welcome with nick") {
    assert(parser.isEmpty(session.spawnClient(Client.CHLOE).nick()))
  }

  test("no unexpected welcome with user") {
    assert(parser.isEmpty(session.spawnClient(Client.CHLOE).user()))
  }

  test("no unexpected welcome with multiple nicks") {
    assert(parser.isEmpty(session.spawnClient(Client.CHLOE).nick()))
    assert(parser.isEmpty(session.spawnClient(Client.KATE).nick()))
  }

  test("no unexpected welcome with multiple user") {
    assert(parser.isEmpty(session.spawnClient(Client.CHLOE).user()))
    assert(parser.isEmpty(session.spawnClient(Client.KATE).user()))
  }
}
