package rtjvm

import cats.effect.std.Semaphore
import cats.effect.{IO, IOApp}
import utils.IO_Ops

import scala.concurrent.duration.DurationInt
import scala.util.Random

object Semaphores extends IOApp.Simple {

  // semaphore has permits which controls the # of threads that can access a certain piece of code

  val nPermits = 2
  val semaphore: IO[Semaphore[IO]] = Semaphore[IO](nPermits)

  // example: limiting # of concurrent sessions on a server
  def doWorkWhileLoggeedIn(): IO[Int] = IO.sleep(1.second) >> IO(Random.nextInt(100))

  def login(id: Int, sem: Semaphore[IO]): IO[Int] = for {
    _ <- IO(s"[session $id] waiting to log in...").Debug
    _ <- sem.acquire
    // critical section
    _ <- IO(s"[session $id] logged in, working...").Debug
    res <- doWorkWhileLoggeedIn()
    _ <- IO(s"[session $id] done: $res, logging out...").Debug
    _ <- sem.release
  } yield res

  def demoSemaphore() = for {
    sem <- Semaphore[IO](2)
    user1Fib <- login(1, sem).start
    user2Fib <- login(2, sem).start
    user3Fib <- login(3, sem).start
    _ <- user1Fib.join
    _ <- user2Fib.join
    _ <- user3Fib.join
  } yield ()

  override def run: IO[Unit] = demoSemaphore()
}
