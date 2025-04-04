package rtjvm

import cats.effect.IO

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

// pure functional programming
// substitution = replace expression with the value and the meaning remains the same

object Effects {

  def combine(left: Int, right: Int): Int = left + right

  // referential transparency - replace expression with value = behaviour will be the same
  val five = combine(2, 3) // same as fiveAgain
  val fiveAgain = 2 + 3
  val fiveAgainAgain = 5

  // broken RT - not identical program, impure program
  val printSomething: Unit = println("cats effect")
  val printSomething_v2: Unit = () // replaced with the value of printing, but the behaviour changed :(

  // example: change a variable

  var anInt = 0
  val changingVar = anInt += 1

  // effect - making side effect pure to properly do RT

  /**
   * Effect types
   * Properties:
   * (1) type signature describes the kind of calculation that will be performed
   * (2) type signature describes the value that will be calculated
   * (3) when side effects are needed, effect construction is separate from effect execution
   */

    // example: Option
    // - possibly absent value
    // - computes a value of type A, if it exists
    // (1): checks
    // (2): checks
    // (3): checks because side effects are not needed to construct the option of int
  val anOption: Option[Int] = Option(42)

  // Future
  // (1) describes an async computation: checks
  // (2) computes a value of type A if it's successful: checks
  // (3) side effect is required (allocating or scheduling a thread): fails
  import scala.concurrent.ExecutionContext.Implicits.global
  val aFuture: Future[Int] = Future(42)

  // example
  // any computation that might produce side effects
  // check
  // check, IO creation is not the same as running that IO
  private case class io[A] private (unsafeRun: () => A) {
    def map[B](f: A => B): io[B] = new io[B](() => f(unsafeRun()))
    def flatMap[B](f: A => io[B]): io[B] = new io[B](() => f(unsafeRun()).unsafeRun())
  }

  private object io {
    def get[A](thunk: => A): io[A] = new io[A](() => thunk)
  }

  def print: IO[Unit] = IO.println("cats effect")

  def main(args: Array[String]): Unit = {

  }


}
