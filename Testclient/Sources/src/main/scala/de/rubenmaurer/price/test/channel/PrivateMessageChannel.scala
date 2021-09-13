package de.rubenmaurer.price.test.channel

import de.rubenmaurer.price.core.facade.{Client, Parser, Session}
import de.rubenmaurer.price.test.BaseTestSuite
import de.rubenmaurer.price.util.Channel

class PrivateMessageChannel(session: Session, parser: Parser, testName: String) extends BaseTestSuite(session, parser, testName) {
  test("private message on channel") {
    val message: String = "I love you"
    val channel: Channel = Channel.BLACKWELL_ART
    val max: Client = session.spawnClient(Client.MAX).authenticate().join(channel)
    val chloe: Client = session.spawnClient(Client.CHLOE).authenticate().join(channel)

    assert(parser.isPrivateMessage(
      max.await(() => chloe.privateMessage(channel, message)), message
    ))
  }

  test("private message while not in channel") {
    val channel: Channel = Channel.BLACKWELL_ART

    session.spawnClient(Client.KATE).authenticate().join(channel)
    val max: Client = session.spawnClient(Client.MAX).authenticate().privateMessage(channel, "Hello there!", 1)

    assert(parser.isCannotSendToChannel(max, channel))
  }

  test("private message to non existing channel") {
    val channel: Channel = Channel.BLACKWELL_ART
    val max: Client = session.spawnClient(Client.MAX).authenticate().privateMessage(channel, "Hello there!", 1)

    assert(parser.isNoSuchChannel(max, channel))
  }

  test("notice to non existing channel") {
    val channel: Channel = Channel.DINER
    val max: Client = session.spawnClient(Client.MAX).authenticate().notice(channel, "Hello there!")

    assert(parser.isEmpty(max))
  }
}
