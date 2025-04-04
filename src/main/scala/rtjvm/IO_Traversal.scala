package rtjvm

import cats.effect.{IO, IOApp}
import cats.implicits.{catsSyntaxApplicativeId, toTraverseOps}
import rtjvm.IO_Traversal.workload

import scala.collection.LazyZip2
import scala.concurrent.Future

object IO_Traversal extends IOApp.Simple {

  import scala.concurrent.ExecutionContext.Implicits.global

  def heavyComputation(string: String): Future[Int] = Future {
    Thread sleep scala.util.Random.nextInt(1000)

    string.split(" ").length
  }

  val workload: List[String] = List(
    "I quite like CE! <3",
    "Scala is great ;) ",
    "Looking forward to some awesome stuff :D",
  )
  val futures: List[Future[Int]] = workload.map(heavyComputation)
  // Future[List[Int]] would be hard to obtain - traverse

  // traverse
  import cats.Traverse
  import cats.instances.list._
  // ^^ this stores ALL the result

  def clunkyFutures(): Unit = {
    val workload: List[String] = List(
      "I quite like CE! <3",
      "Scala is great ;) ",
      "Looking forward to some awesome stuff :D",
    )
    val futures: List[Future[Int]] = workload.map(heavyComputation)

    futures.foreach(_.foreach(println))
  }

  def traverseFutures(): Unit = {
    val workload: List[String] = List(
      "I quite like CE! <3",
      "Scala is great ;) ",
      "Looking forward to some awesome stuff :D",
    )

    val listTraverse = Traverse[List]
    val singleFuture: Future[List[Int]] = listTraverse.traverse(workload)(heavyComputation)
    singleFuture.foreach(println)
  }

  // traverse for IO
  def computeAsIO(string: String): IO[Int] = IO delay {
    Thread sleep scala.util.Random.nextInt(1000)
    string.split(" ").length
  }

  val ios: List[IO[Int]] = workload.map(computeAsIO)
  val singleIO: IO[List[Int]] = Traverse[List].traverse(workload)(computeAsIO)

  // parallel traversal
  import cats.syntax.parallel._
  val parallelSingleIO: IO[List[Int]] = workload.parTraverse(computeAsIO)

  // exercises
  def sequence[A](ios: List[IO[A]]): IO[List[A]] = {
    val f: IO[List[A]] = ios.traverse(identity)
    val d: IO[List[A]] = ios.sequence

    d
  }

  override val run: IO[Unit] = IO.unit
}
