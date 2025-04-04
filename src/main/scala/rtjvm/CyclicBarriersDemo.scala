package rtjvm

import cats.effect.std.CyclicBarrier
import cats.effect.{IO, IOApp}
import cats.implicits.catsSyntaxParallelTraverse1
import utils.IO_Ops

import scala.concurrent.duration.DurationInt
import scala.util.Random

object CyclicBarriersDemo extends IOApp.Simple {

  /**
   * A cyclic barries is a coordination primitive that
   *  - is initialized with a count
   *  - has a isngle API: await
   *
   *  A cyclic barries will (semantically) block all fibers calling its await() method until we have exatly N fibers waiting,
   *  at which point the barrier will unblock all fibers and reset to its original state.
   *
   *  Any further fiber will again block until we have exactly N fibers waiting.
   */

  def createUser(id: Int, barrier: CyclicBarrier[IO]): IO[Unit] = for {
    _ <- IO.sleep((Random.nextDouble * 500).toInt.millis)
    _ <- IO(s"[user $id] Just heard there's a new social network - signing up for the waitlist...").Debug
    _ <- IO.sleep((Random.nextDouble * 1500).toInt.millis)
    _ <- IO(s"[user $id] On the waitlist now, can't wait!").Debug
    _ <- barrier.await // semantically block the fiber when there are exactly N users waiting
    _ <- IO(s"[user $id] OMG this is so cool!").Debug
  } yield ()

  def openNetwork(): IO[Unit] = for {
    _ <- IO("[announcer] The Rock the JVM social network is up for registration! Launching when we have 10 users!")
    barrier <- CyclicBarrier[IO](10)
    _ <- (1 to 14).toList.parTraverse { n => createUser(n, barrier) }
  } yield ()

  override def run: IO[Unit] = openNetwork()
}
