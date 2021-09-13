package de.rubenmaurer.price.test.motd

import de.rubenmaurer.price.core.facade.{Client, Parser, Session}
import de.rubenmaurer.price.test.BaseTestSuite
import de.rubenmaurer.price.util.Channel

import java.io.{File, PrintWriter}

class MessageOfTheDay(session: Session, parser: Parser, testName: String) extends BaseTestSuite(session, parser, testName) {
  test("list user") {
    assert(parser.isLUser(session.spawnClient(Client.CHLOE).authenticate()))
  }

  test("list multiple user") {
    assert(parser.isLUser(session.spawnClient(Client.KATE).authenticate()))
    assert(parser.isLUser(session.spawnClient(Client.CHLOE).authenticate(), clients = 2, users = 2))
  }

  test("list multiple user with unregistered") {
    session.spawnClient(Client.CHLOE)
    assert(parser.isLUser(session.spawnClient(Client.MAX).authenticate(), users = 2, unknown = 1))
  }

  test("lusers") {
    assert(parser.isLUser(session.spawnClient(Client.RACHEl).authenticate().lusers()))
  }

  test("lusers for multiple users") {
    session.spawnClient(Client.CHLOE).authenticate()
    session.spawnClient(Client.MAX).authenticate()
    val rachel: Client = session.spawnClient(Client.RACHEl).authenticate()

    assert(parser.isLUser(rachel.lusers(), clients = 3, users = 3))
  }

  test("lusers with channels") {
    session.spawnClient(Client.MAX).authenticate().join(Channel.BLACKWELL)
    assert(parser.isLUser(session.spawnClient(Client.KATE).authenticate(), clients = 2, users = 2, channels = 1))
  }

  test("lusers mixed") {
    session.spawnClient(Client.KATE).authenticate().join(Channel.DINER)
    session.spawnClient(Client.RACHEl).authenticate().join(Channel.DINER)
    session.spawnClient(Client.MAX).authenticate().join(Channel.BLACKWELL_ART)

    assert(parser.isLUser(session.spawnClient(Client.CHLOE).authenticate().lusers(), clients = 4, users = 4, channels = 2))
  }

  test("no message of the day") {
    assert(parser.isNoMessageOfTheDay(session.spawnClient(Client.MAX).authenticate()))
  }

  test("message of the day") {
    val message = "Hello there!"
    new PrintWriter("motd.txt") { write(message); close() }

    assert(parser.isMessageOfTheDay(session.spawnClient(Client.KATE).authenticate().motd(), message))
    new File("motd.txt").delete()
  }
}
