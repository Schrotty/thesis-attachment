package de.rubenmaurer.price.test.connection

import de.rubenmaurer.price.core.facade.{Client, Parser, Session}
import de.rubenmaurer.price.test.BaseTestSuite

class MultiUserConnection(session: Session, parser: Parser, testName: String) extends BaseTestSuite(session, parser, testName) {
  test("two users connecting") {
    assert(parser.isWelcome(session.spawnClient(Client.KATE).authenticate()))
    assert(parser.isWelcome(session.spawnClient(Client.CHLOE).authenticate()))
  }

  test("two users with identical nickname") {
    assert(parser.isWelcome(session.spawnClient(Client.MAX).authenticate()))
    assert(parser.isNicknameInUse(session.spawnClient(Client.MAX.copy).authenticateWithUsedNickname()))
  }
}
