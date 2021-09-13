import Spawner.child
import akka.actor.testkit.typed.Effect.Spawned
import akka.actor.testkit.typed.scaladsl.{BehaviorTestKit, TestInbox}
import org.scalatest.funsuite.AnyFunSuite

class Synced extends AnyFunSuite {
  test("send message") {
    val testKit = BehaviorTestKit(Welcome())
    val inbox = TestInbox[String]()

    testKit.run(inbox.ref)
    inbox.expectMessage("Welcome!")
  }

  test("spawn children") {
    val testKit = BehaviorTestKit(Spawner())
    testKit.run("spawn")

    testKit.expectEffect(Spawned(child, "welcome"))
  }
}
