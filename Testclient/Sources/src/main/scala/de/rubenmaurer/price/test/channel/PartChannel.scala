package de.rubenmaurer.price.test.channel

import de.rubenmaurer.price.core.facade.{Client, Parser, Session}
import de.rubenmaurer.price.test.BaseTestSuite
import de.rubenmaurer.price.util.Channel

class PartChannel(session: Session, parser: Parser, testName: String) extends BaseTestSuite(session, parser, testName) {
  test("join and part channel") {
    val channel: Channel = Channel.BLACKWELL_SCIENCE
    val chloe: Client = session.spawnClient(Client.CHLOE).authenticate().join(channel).part(channel)

    assert(parser.isPart(chloe, channel))
  }

  test("join and part channel with message") {
    val channel: Channel = Channel.DINER
    val kate: Client = session.spawnClient(Client.KATE).authenticate().join(channel).part(channel, "Bye!")

    assert(parser.isPart(kate, channel))
  }

  test("part channel not existing") {
    val channel: Channel = Channel.BLACKWELL_ART
    val max: Client = session.spawnClient(Client.MAX).authenticate().part(channel, "Bye!")

    assert(parser.isNoSuchChannel(max, channel))
  }

  test("part not in channel") {
    val channel: Channel = Channel.BLACKWELL

    session.spawnClient(Client.KATE).authenticate().join(channel)
    val rachel: Client = session.spawnClient(Client.RACHEl).authenticate().part(channel, "Bye!")

    assert(parser.isNotOnChannel(rachel, channel))
  }
}
