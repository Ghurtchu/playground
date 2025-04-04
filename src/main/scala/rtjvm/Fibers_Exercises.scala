package rtjvm

import cats.effect
import cats.effect.kernel.Outcome
import cats.effect.{IO, IOApp}
import utils.IO_Ops

import scala.concurrent.duration.{DurationInt, FiniteDuration}

object Fibers_Exercises extends IOApp.Simple {

  // ex1
  def exercise_1[A](io: IO[A]): IO[A] = {
    (for {
      fiber <- io.start
      result <- fiber.join
    } yield result).flatMap {
      case Outcome.Succeeded(fa) => fa
      case Outcome.Errored(e) => IO.raiseError(e)
      case Outcome.Canceled() => IO.raiseError(new RuntimeException("computation cancelled"))
    }
  }

  // ex2
  def exercise_2[A, B](ioA: IO[A], ioB: IO[B]): IO[(A, B)] = {
    val result: IO[(Outcome[IO, Throwable, A], Outcome[IO, Throwable, B])] = for {
      fiberA <- ioA.start
      fiberB <- ioB.start
      resultA <- fiberA.join
      resultB <- fiberB.join
    } yield resultA -> resultB

    result.flatMap {
      case (Outcome.Succeeded(fa), Outcome.Succeeded(fb)) => fa.flatMap(a => fb.map(b => (a, b)))
      case (Outcome.Errored(e), _) => IO.raiseError(e)
      case (_, Outcome.Errored(e)) => IO.raiseError(e)
      case (Outcome.Canceled(), _) => IO.raiseError(new RuntimeException("F[A] cancelled"))
      case (_, Outcome.Canceled()) => IO.raiseError(new RuntimeException("F[B] cancelled"))
    }
  }

  def testEx2() = {
    val firstIO = IO.sleep(2.seconds) >> IO(1).Debug
    val secondIO = IO.sleep(3.seconds) >> IO(2).Debug

    exercise_2(firstIO, secondIO).Debug.void
  }

  def timeout[A](io: IO[A], duration: FiniteDuration): IO[A] = {
    val computation = for {
      fib <- io.start
      _ <- IO.sleep(duration) >> fib.cancel
      result <- fib.join
    } yield result

    computation.flatMap {
      case Outcome.Succeeded(fa) => fa
      case Outcome.Errored(e) => IO raiseError e
      case Outcome.Canceled() => IO raiseError new RuntimeException("Computation canceled.")
    }
  }

  def testEx3(): Unit = {
    val aComputation = IO("Starting").Debug >> IO.sleep(1.second) >> IO("Done!").Debug >> IO.pure(42)

    timeout(aComputation, 2.seconds).Debug.void
  }

  override def run: IO[Unit] = IO.unit
}
