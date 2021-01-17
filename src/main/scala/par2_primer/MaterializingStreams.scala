package par2_primer

import akka.actor.ActorSystem

import scala.util.{Failure, Success}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}

object MaterializingStreams extends App {
  implicit val system = ActorSystem("MaterializingStreams")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  val simpleGraph = Source(1 to 10).to(Sink.foreach(println))
//  val simpleMaterializedValue = simpleGraph.run()

  val source = Source(1 to 10)
  val sink = Sink.reduce[Int]((a, b) => a + b)
  val someFuture = source.runWith(sink)
  someFuture.onComplete {
    case Success(value) => println(s"The sum of all elements is: $value")
    case Failure(exception) => println(s"The sum of the elements could not be completed: ${exception.getMessage}")
  }

  // choosing materialized values
  val simpleSource = Source(1 to 10)
  val simpleFlow = Flow[Int].map(x => x + 1)
  val simpleSink = Sink.foreach(println)
  val graph1 = simpleSource.viaMat(simpleFlow)((sourceMat, flowMat) => flowMat).toMat(simpleSink)(Keep.right)
  val graph2 = simpleSource.viaMat(simpleFlow)(Keep.right).toMat(simpleSink)(Keep.right)
  graph2.run().onComplete{
    case Success(_) => println("Stream processing finished")
    case Failure(ex) => println(s"Stream processing failed due to: ${ex.getMessage}")
  }

  system.terminate()
}
