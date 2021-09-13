import de.rubenmaurer.price.core.facade.Client
import de.rubenmaurer.price.core.facade.Client.Response
import org.scalatest.PrivateMethodTester
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.{convertToAnyShouldWrapper, equal}
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.prop.{TableFor1, TableFor2, TableFor3}

/**
 * Tests for the clients log.
 */
class LogTest extends AnyFunSuite with PrivateMethodTester {
  val transform: PrivateMethod[Unit] = PrivateMethod[Unit](Symbol("received"))

  val messages: TableFor1[String] = Table("Hello there!", "Default", "Basic Message")
  val mTac: TableFor2[String, String] = Table(
    ("d", "key"),
    ("Specific msg responder", "respond"),
    ("Inside Voices Outside Voices", "side"),
    ("Internet Relay Chat", "lay")
  )

  val codes: TableFor2[String, Int] = Table(
    ("d", "code"),
    ("Hello there!", 542),
    ("Default", 966),
    ("Basic", 124)
  )

  val codesWithKey: TableFor3[String, Int, String] = Table(
    ("d", "code", "key"),
    ("Hello there!", 542, "Hell"),
    ("Default", 966, "fault"),
    ("Basic", 124, "sic")
  )

  test("get last log entry") {
    val max = Client.MAX

    forAll(messages) { d =>
      max invokePrivate transform(Response(d))
      max.log.last should equal(d)
    }
  }

  test("get log entry with startWith") {
    val max = Client.MAX

    forAll(messages) { d =>
      max invokePrivate transform(Response(d))
      max.log.startWith(d.substring(0, 1)) should equal(d)
    }
  }

  test("find log entry") {
    val max = Client.MAX

    forAll(mTac) { (d, key) =>
      max invokePrivate transform(Response(d))
      max.log.find(key) should equal(d)
    }
  }

  test("find entry by code") {
    val max = Client.MAX

    forAll(codes) { (d, code) =>
      val str = s"$d - $code"
      max invokePrivate transform(Response(str))
      max.log.byCode(code) should equal(str)
    }
  }

  test("find entry by code and keyword") {
    val max = Client.MAX

    forAll(codesWithKey) { (d, code, key) =>
      val str = s"$d - $code"
      max invokePrivate transform(Response(str))
      max.log.byCodeAnd(code, key) should equal(str)
    }
  }
}
