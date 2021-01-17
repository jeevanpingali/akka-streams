package part1_recap

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor.{Actor, ActorLogging, ActorSystem, OneForOneStrategy, PoisonPill, Props, Stash, SupervisorStrategy}
import akka.util.Timeout

object AkkaRecap extends App {
  class SimpleActor extends Actor with Stash with ActorLogging  {
    override def receive: Receive = {
      case "createChild" =>
        val childActor = context.actorOf(Props[SimpleActor], "myChild")
        childActor ! "Hello"
      case "stashThis" =>
         stash()
      case "change handler now" =>
        unstashAll()
        context.become(anotherHandler)
      case "change" => context.become(anotherHandler)
      case message =>
        println(s"I received $message")
    }

    def anotherHandler: Receive = {
      case message => println(s"In another receive method: $message")
    }

    override def preStart(): Unit = {
      println("I'm starting")
      log.info("I'm starting")
    }

    override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
      case _: RuntimeException => Restart
      case _ => Stop
    }
  }

  // actor encapsulation
  val system = ActorSystem("AkkaRecap")
  // #1: you can instantiate an actor through the actor system
  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")
  // #2: sending messages
  simpleActor ! "Hello"

  /*
    - messages are send async
    - many actors in millions can share a few dozen threads
    - each message is processed or handled ATOMICALLY
    - no need for locks
   */

  // 4: changing actor behavior + stashing and unstashing


  // 5: actors can spawn another actors

  // 6: akka create three guardian actors - /system, /user, / guardians

  // 7: actors have a defined lifecycle: started, stopped, suspended, resumed, restarted

  // 8: lifecycle hooks

  // 9: stopping themselves

  simpleActor ! PoisonPill

  // 10: logging by mixing in ActorLogging

  // 11: supervison

  // 12: configure Akka infrastructure: dispatchers, routers, mailboxes

  // 13: schedulers and timers

  import scala.concurrent.duration._
  import system.dispatcher

  system.scheduler.scheduleOnce(2 seconds) {
    simpleActor ! "delayed birthday"
  }

  // 14: Akka patterns including FSM + ask pattern
  import  akka.pattern.ask
  implicit val timeout = Timeout(3 seconds)
  val future = simpleActor ? "question"

  // the pipe patter
  import akka.pattern.pipe
  val anotherActor = system.actorOf(Props[SimpleActor], "anotherSimpleActor")
  future.mapTo[String].pipeTo(anotherActor)

  system.terminate()

}
