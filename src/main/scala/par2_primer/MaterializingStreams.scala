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

  // materialized - sugars
  val sum = Source(1 to 10).runWith(Sink.reduce[Int](_ + _))
  sum.onComplete{
    case Success(sumValue) => println(s"Sum is $sumValue")
    case Failure(ex) => println(s"Stream processing failed due to: ${ex.getMessage}")
  }

  val sum2 = Source(1 to 10).runReduce[Int](_ + _) // even shorter version
  sum2.onComplete{
    case Success(sumValue) => println(s"Sum2 is $sumValue")
    case Failure(ex) => println(s"Stream processing failed due to: ${ex.getMessage}")
  }

  // backwards
  Sink.foreach[Int](println).runWith(Source.single(42))

  // both ways
  Flow[Int].map(x => 2 * x).runWith(simpleSource, simpleSink)

  /**
   * - return the last element out of a source (use Sink.last)
   * - compute the total word count out of a stream of sentences
   *  - map, fold, reduce
   */
  val f1 = Source(1 to 10).toMat(Sink.last)(Keep.right).run()
  val f2 = Source(1 to 10).runWith(Sink.last)

  val sentenceSource = Source(List("Akka is awesome", "I love streams", "Materialzed values are whatever"))

  val wordCountSink = Sink.fold[Int, String](0)((currentWords, newSentence) => currentWords + newSentence.split(" ").length)
  val g1 = sentenceSource.toMat(wordCountSink)(Keep.right).run()
  val g2 = sentenceSource.runWith(wordCountSink)
  val g3 = sentenceSource.runFold(0)((currentWords, newSentence) => currentWords + newSentence.split(" ").length)

  val wordCountFlow = Flow[String].fold[Int](0)((currentWords, newSentence) => currentWords + newSentence.split(" ").length)
  val g4 = sentenceSource.via(wordCountFlow).toMat(Sink.head)(Keep.right).run()
  val g5 = sentenceSource.viaMat(wordCountFlow)(Keep.right).toMat(Sink.head)(Keep.right).run()
  val g6 = sentenceSource.via(wordCountFlow).runWith(Sink.head)
  val g7 = wordCountFlow.runWith(sentenceSource, Sink.head)._2

  system.terminate()
}
