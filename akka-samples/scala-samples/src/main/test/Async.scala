import Echo.EchoMessage
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers.include
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class Async extends AnyFunSuite with BeforeAndAfterAll {
  val kit: ActorTestKit = ActorTestKit()

  override def afterAll(): Unit = kit.shutdownTestKit()

  test("send message") {
    val welcome = kit.spawn(Welcome())
    val probe = kit.createTestProbe[String]()

    welcome ! probe.ref
    probe.expectMessage("Welcome!")
  }

  test("stop actor") {
    val welcome = kit.spawn(Welcome())
    val probe = kit.createTestProbe[String]()

    kit.stop(welcome)
    probe.expectTerminated(welcome)
  }

  test("echo message") {
    val echo = kit.spawn(Echo())
    val probe = kit.createTestProbe[EchoMessage]()

    echo ! EchoMessage("welcome", probe.ref)
    probe.receiveMessage().message should include("welcome")
  }
}
