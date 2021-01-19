package par2_primer

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}

object BackpressureBasics extends App {
  implicit val system = ActorSystem("BackpressureBasics")
  implicit val materilizer = ActorMaterializer

  val fastSource = Source(1 to 1000)
  val slowSink = Sink.foreach[Int] { x=>
    // simulate a long process
    Thread.sleep(1000)
    println(s"Sink: $x")
  }

  val regularSink = Sink.foreach[Int] { x=>
    println(s"Sink: $x")
  }

//  fastSource.to(slowSink).run() // not backpressure

//  fastSource.async.to(slowSink).run() // backpressure in place

  val simpleFlow = Flow[Int].map { x =>
    println(s"Incoming: $x")
    x + 1
  }

//  fastSource.async.via(simpleFlow).async.to(slowSink).async.run()

  /*
  reactions to backpressure (in order):
  - try to slow down if possible
  - to buffer elements until there is more demand
  - drop down elements from the buffer, if it overflows
  - kill the whole stream (failure)
   */

  val bufferedFlow = simpleFlow.buffer(10, overflowStrategy = OverflowStrategy.dropHead)
//  fastSource.async.via(bufferedFlow).async.to(slowSink).async.run()


  // throttling
  import scala.concurrent.duration._
  fastSource.throttle(100, 1 second).runWith(regularSink)

  /**
   * Recap
   * - data flows through streams in response to demand
   * - Akka streams can slow down fast producers (by emitting backpressure signals
   * - Backpressure procotol is transparent ( can do buffer overflow as much)
   */
}