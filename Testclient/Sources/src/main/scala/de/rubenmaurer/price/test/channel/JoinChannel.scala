package de.rubenmaurer.price.test.channel

import de.rubenmaurer.price.core.facade.{Client, Parser, Session}
import de.rubenmaurer.price.test.BaseTestSuite
import de.rubenmaurer.price.util.Channel

class JoinChannel(session: Session, parser: Parser, testName: String) extends BaseTestSuite(session, parser, testName) {
  test("join channel") {
    val max: Client = session.spawnClient(Client.MAX).authenticate()

    assert(parser.isJoin(max.join(Channel.BLACKWELL), Channel.BLACKWELL, max))
  }

  test("join already joined channel") {
    val chloe: Client = session.spawnClient(Client.CHLOE).authenticate().join(Channel.BLACKWELL)
    chloe.log.clearPlain()

    assert(parser.isEmpty(chloe.join(Channel.BLACKWELL)))
  }

  test("multiple joins") {
    val channel: Channel = Channel.DINER
    val kate: Client = session.spawnClient(Client.KATE).authenticate().join(channel)
    val chloe: Client = session.spawnClient(Client.CHLOE).authenticate().join(channel)
    val max: Client = session.spawnClient(Client.MAX).authenticate().join(channel)

    assert(parser.isJoin(kate, channel, kate) &&
      parser.isJoin(chloe, channel, kate, chloe) &&
      parser.isJoin(max, channel, kate, chloe, max))
  }
}
