package par2_primer

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

object OperatorFusion extends App {
  implicit val system = ActorSystem("OperatorFusion")
  implicit val materilizer = ActorMaterializer

  val simpleSource = Source(1 to 1000)
  val simpleFlow = Flow[Int].map(_ + 1)
  val simpleFlow2 = Flow[Int].map(_ * 10)
  val simpleSink = Sink.foreach[Int](println)

  // this runs on the SAME actor
//  simpleSource.via(simpleFlow).via(simpleFlow2).to(simpleSink).run()
  // operator/component fusion - default behavior

  val complexFlow = Flow[Int].map { x =>
    // simulate a long operation
    Thread.sleep(1000)
    x + 1
  }

  val complexFlow2 = Flow[Int].map { x =>
    // simulate a long operation
    Thread.sleep(1000)
    x * 10
  }

//  simpleSource.via(complexFlow).via(complexFlow2).to(simpleSink).run()

  // async boundary

/*  simpleSource.async
    .via(complexFlow).async // runs on one actor
    .via(complexFlow2).async // runs on another actor
    .to(simpleSink).async // runs on a third actor
    .run()*/

  // ordering guarantees - with or without async boundaries

  Source(1 to 3).async
    .map(element => {println(s"Flow A: $element"); element }).async
    .map(element => {println(s"Flow B: $element"); element }).async
    .map(element => {println(s"Flow C: $element"); element }).async
    .runWith(Sink.ignore)

//  system.terminate()
}
