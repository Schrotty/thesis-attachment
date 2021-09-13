package de.rubenmaurer.price.test.whois

import de.rubenmaurer.price.core.facade.{Client, Parser, Session}
import de.rubenmaurer.price.test.BaseTestSuite

class Whois(session: Session, parser: Parser, testName: String) extends BaseTestSuite(session, parser, testName) {
  test("whois") {
    val rachel: Client = session.spawnClient(Client.RACHEl).authenticate()
    val chloe: Client = session.spawnClient(Client.CHLOE).authenticate()

    chloe.whois(rachel)
    assert(parser.isWhois(chloe, rachel))
  }

  test("whois with unknown user") {
    val chloe: Client = session.spawnClient(Client.CHLOE).authenticate()

    chloe.whois(Client.RACHEl, shouldFail = true)
    assert(parser.isNoSuchNick(chloe, Client.RACHEl))
  }
}
