package de.rubenmaurer.price.test.channel

import de.rubenmaurer.price.core.facade.{Client, Parser, Session}
import de.rubenmaurer.price.test.BaseTestSuite
import de.rubenmaurer.price.util.Channel

class WhoChannel(session: Session, parser: Parser, testName: String) extends BaseTestSuite(session, parser, testName) {
  test("who is channel") {
    val kate: Client = session.spawnClient(Client.KATE).authenticate().join(Channel.BLACKWELL_ART)
    session.spawnClient(Client.RACHEl).authenticate().join(Channel.BLACKWELL_ART)
    session.spawnClient(Client.MAX).authenticate().join(Channel.DINER)

    assert(parser.isWho(kate.who(Channel.BLACKWELL_ART, 2), Channel.BLACKWELL_ART, List(kate, Client.RACHEl): _*))
  }

  test("who is all") {
    val rachel: Client = session.spawnClient(Client.RACHEl).authenticate().join(Channel.DINER)
    session.spawnClient(Client.MAX).authenticate().join(Channel.DINER)
    session.spawnClient(Client.KATE).authenticate()

    assert(parser.isWho(rachel.who("*", 1), Channel.ALL, List(Client.KATE): _*))
  }
}
