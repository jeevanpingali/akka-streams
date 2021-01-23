package part3_graphs

/**
 * write complex Akka Streams Graphs
 * familiarize with Graph DSL
 * non-linear components:
 * fan-in
 * fan-out
 */

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Source, Zip}

object GraphBasics extends App {
  implicit val system = ActorSystem("GraphicsBasics")
  implicit val materilizer = ActorMaterializer

  val input = Source(1 to 1000)
  val incrementer = Flow[Int].map(x => x + 1)
  val multiplier = Flow[Int].map(x => x * 10)
  val output = Sink.foreach[(Int, Int)](println)

  val graph = RunnableGraph.fromGraph(
    GraphDSL.create() {
      implicit builder: GraphDSL.Builder[NotUsed] =>
        import GraphDSL.Implicits._

        val broadcast = builder.add(Broadcast[Int](2))
        val zip = builder.add(Zip[Int, Int])

        input ~> broadcast

        broadcast.out(0) ~> incrementer ~> zip.in0
        broadcast.out(1) ~> multiplier ~> zip.in1

        zip.out ~> output

        ClosedShape
    }
  )

  //  graph.run()

  /**
   * exercise 1: feed a source into 2 sinks at the same time (hint: use a broadcast)
   */

  val firstSink = Sink.foreach[Int](x => println(s"First sink: $x"))
  val secondSink = Sink.foreach[Int](x => println(s"Second sink: $x"))

  val graph2 = RunnableGraph.fromGraph(
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val broadcast = builder.add(Broadcast[Int](2))

      input ~> broadcast
      broadcast.out(0) ~> firstSink
      broadcast.out(1) ~> secondSink

      ClosedShape
    }
  )

  graph2.run()

}
