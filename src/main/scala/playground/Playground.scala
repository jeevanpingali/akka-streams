package playground

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import scala.concurrent.duration._

object Playground extends App {
  implicit val system = ActorSystem("Playground")
  implicit val  materializer = ActorMaterializer()

  Source.single("Hello Streams").to(Sink.foreach(println)).run()

  import system.dispatcher
  system.scheduler.scheduleOnce(10 seconds) (
    system.terminate()
  )
}
