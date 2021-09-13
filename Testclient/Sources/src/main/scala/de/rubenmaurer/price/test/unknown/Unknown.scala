package de.rubenmaurer.price.test.unknown

import de.rubenmaurer.price.core.facade.{Client, Parser, Session}
import de.rubenmaurer.price.test.BaseTestSuite

class Unknown(session: Session, parser: Parser, testName: String) extends BaseTestSuite(session, parser, testName) {
  test("sending unknown command") {
    val rachel: Client = session.spawnClient(Client.RACHEl).authenticate()
    rachel.send("UNKNOWN", 1)

    assert(parser.isUnknown(rachel, "UNKNOWN"))
  }
}
