import akka.NotUsed
import akka.stream.ClosedShape
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, RunnableGraph, Sink, Source}

val graph = RunnableGraph.fromGraph(GraphDSL.create() {
  implicit builder: GraphDSL.Builder[NotUsed] =>
    import GraphDSL.Implicits._
    val source = Source(1 to 25)
    val sink = Sink.ignore

    val split = builder.add(Broadcast[Int](2))
    val merge = builder.add(Merge[Int](2))

    val f1, f2, f3 = Flow[Int].map(_ + 1)
    val f4 = Flow[Int].map(_ * 2)

    source ~> f1 ~> split ~> f2 ~> merge ~> f3 ~> sink
    split ~> f4 ~> merge
  ClosedShape
})