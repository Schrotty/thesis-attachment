package de.rubenmaurer.price.test.channel

import de.rubenmaurer.price.core.facade.{Client, Parser, Session}
import de.rubenmaurer.price.test.BaseTestSuite
import de.rubenmaurer.price.util.Channel

class TopicChannel(session: Session, parser: Parser, testName: String) extends BaseTestSuite(session, parser, testName) {
  test("set topic") {
    val channel: Channel = Channel.BLACKWELL_ART
    val topic: String = "Hello there!"

    val kate: Client = session.spawnClient(Client.KATE).authenticate().join(channel).topic(channel, topic)
    assert(parser.isTopic(kate, channel, topic))
  }

  test("get topic") {
    val topic: String = "Default Channel"
    val channel: Channel = Channel.BLACKWELL
    val max: Client = session.spawnClient(Client.MAX).authenticate().join(channel).topic(channel, topic).topic(channel)

    assert(parser.isTopic(max, channel, topic))
  }

  test("get 'no topic set'") {
    val channel: Channel = Channel.DINER
    val kate: Client = session.spawnClient(Client.KATE).authenticate().join(channel).topic(channel)

    assert(parser.isNoTopicSet(kate, channel))
  }

  test("not on channel") {
    val channel: Channel = Channel.BLACKWELL_SCIENCE
    val rachel: Client = session.spawnClient(Client.RACHEl).authenticate().topic(channel)

    assert(parser.isNotOnChannel(rachel, channel))
  }
}
