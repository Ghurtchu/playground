package rtjvm

import cats.effect.std.Mutex
import cats.effect.{IO, IOApp}
import cats.implicits.catsSyntaxParallelTraverse1
import utils.IO_Ops

import scala.concurrent.duration.DurationInt

object MutexExample extends IOApp.Simple {

  def program(mutex: Mutex[IO]): IO[Unit] =
    for {
      _ <- IO("START").Debug
      _ <- mutex.lock.surround {
        IO("task started").Debug >> IO.sleep(1.second) >> IO("task finished").Debug
      }
      _ <- IO("END").Debug
    } yield ()

  def tenConcurrentPrograms(mutex: Mutex[IO]): IO[List[Unit]] = (1 to 10).toList.parTraverse { _ =>
    program(mutex)
  }


  override def run: IO[Unit] = Mutex[IO].flatMap(m => tenConcurrentPrograms(m)) >> IO.sleep(3.seconds)
}
