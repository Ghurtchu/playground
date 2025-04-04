package rtjvm

import cats.effect.kernel.Outcome
import cats.effect.{Async, FiberIO, IO, IOApp, OutcomeIO}
import utils.IO_Ops

import scala.concurrent.duration.{DurationInt, FiniteDuration}

object RacingIOs extends IOApp.Simple {

  def runWithSleep[A](value: A, duration: FiniteDuration): IO[A] =
    (IO(s"starting computation: $value").Debug >>
    IO.sleep(duration) >>
    IO(s"computation for $value: done") >>
    IO(value))
      .onCancel(IO(s"computation CANCELED for $value").Debug.void)

  def testRace() = {
    val meaningOfLife = runWithSleep(42, 1.second)
    val favLang = runWithSleep("Scala", 2.second)
    val first: IO[Either[Int, String]] = IO.race(meaningOfLife, favLang)
    // both IOs run on separate fibers and hte first one will complete the result the other one will be canceled

    first.flatMap {
      case Left(mol) => IO(s"Meaning of life won: $mol")
      case Right(lang) => IO(s"Fav lang won: $lang")
    }
  }

  def testRacePair() = {
    val meaningOfLife = runWithSleep(42, 1.second)
    val favLang = runWithSleep("Scala", 2.second)
    // type signature
    // left = winner left, loser fiber handle of right
    // right = winner right, loser fiber handle of left
    val raceResult: IO[Either[(OutcomeIO[Int], FiberIO[String]), (FiberIO[Int], OutcomeIO[String])]] =
      IO.racePair(meaningOfLife, favLang)

    raceResult.flatMap {
      case Left((outMol, fibLang)) => fibLang.cancel >> IO("MOL won: ").Debug >> IO(outMol).Debug
      case Right((fibMol, outLang)) => fibMol.cancel >> IO("Language won").Debug >> IO(outLang).Debug
    }
  }

  /**
   * Exercises:
   * 1 - implement a timeout pattern with race
   */
  def timeout[A](io: IO[A], duration: FiniteDuration): IO[A] =
    IO.race(io, IO.sleep(duration))
      .flatMap {
        case Left(a) => IO(a)
        case Right(value) => IO.raiseError(new RuntimeException("timed out"))
      }

  // 2 - return losing effect, hint: racePair
//  def unrace[A, B](ioa: IO[A], iob: IO[B]): IO[Either[A, B]] = {
//    IO.racePair(ioa, iob)
//      .flatMap {
//        case Left((_, fib)) => fib.join.flatMap {
//          case
//        }
//        case Right((fib, out)) => ???
//      }
//  }

  // 3 - implement race in terms of racePair
  def simpleRace[A, B](ioa: IO[A], iob: IO[B]): IO[Either[A, B]] = ???

  override val run: IO[Unit] = testRace().Debug.void
}
