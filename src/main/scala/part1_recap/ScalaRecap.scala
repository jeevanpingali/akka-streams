package part1_recap

object ScalaRecap extends App {
  val aCondition: Boolean = false

  def myFunction(x: Int) = {
    if(x > 4) 42 else 65
  }

  class Animal
  trait Carnivore {
    def eat(a: Animal): Unit
  }

  object Carnivore

  // generics
  abstract class MyList[+A]

  // method notations, like influx
  1 + 2
  1.+(2)

  // FP
  val anIncrementer: Int => Int = (x: Int) => x + 1
  anIncrementer(1)

  println(List(1,2,3).map(anIncrementer))
}
