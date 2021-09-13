import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Main {
  def main(args: Array[String]): Unit = {
    val source = Source(1 to 25)
    val flow = Flow[Int].map(_ * 2)
    val sink = Sink.fold[Int, Int](0)(_ + _)

    implicit val system: ActorSystem = ActorSystem()
    val sum: Future[Int] = source.via(flow).runWith(sink)

    sum.foreach(s => println(s))
  }
}
