package par2_primer

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.Future

object FirstPrinciples extends App {
  implicit val system = ActorSystem("FirstPrinciples")
  implicit val materilizer = ActorMaterializer()

  // source
  val source = Source(1 to 10)

  // sink
  val sink = Sink.foreach[Int](println)

  val graph = source.to(sink) // this graph is the definition of stream

  graph.run()

  // introduce flows, Akka stream components, to trnasform elements
  val flow = Flow[Int].map(x => x + 1)
  val sourceWithFlow = source.via(flow)
  val flowWithSink = flow.to(sink)

//  sourceWithFlow.to(sink).run()
//  source.to(flowWithSink).run()
//  source.via(flow).to(sink).run()

  // nulls are NOT allowed
//  val illegalSource = Source.single[String](null)
//  illegalSource.to(Sink.foreach(println)).run()
  // Use Option instead

  val finiteSource = Source.single(1)
  val anotherFiniteSource = Source(List(1,2,3))
  val emptySource = Source.empty[Int]
  val infiniteSource = Source(Stream.from(1)) // do not confuse with a "collection" stream

  import scala.concurrent.ExecutionContext.Implicits.global
  val futureSource = Source.fromFuture(Future(42))

  // sinks
  val theMostBoringSink = Sink.ignore
  val theForEachSink = Sink.foreach[String](println)
  val headSink = Sink.head[Int] // receives head element then closes the stream
  val foldSync = Sink.fold[Int, Int](0)((a, b) => a + b)

  // flows - usually map to collection operators
  val mapFlow = Flow[Int].map(x => 2 * x)
  val takeFlow = Flow[Int].take(5) // turns into finite stream, even if the source is infinite
  // drop, filter available
  // NOT have flapMap

  // source to flow -> flow -> ... -> sink
  val doubleFlowGraph = source.via(mapFlow).via(takeFlow).to(sink)
  doubleFlowGraph.run()

  // syntactic sugar
  val mapSource = Source(1 to 10).map(x => x * 2) // equivalent to Source(1 to 10).via(Flow[Int].map(x => x * 2)


  // run streams directly
  mapSource.runForeach(println) // mapSource.to(Sink.forEach[Int](println)).run

  // operators = components

  /**
   * Exercise
   */

  val names = List("First Name", "name", "Second Name", "Third", "Whatever Name")
  val namesSource = Source(names)
  val selectedNames = namesSource.filter(name => name.length > 5).take(2)
  val printSink = selectedNames.runForeach(println)

  system.terminate()
}