package rtjvm

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.catsSyntaxApplicativeId

object IO_Errors extends IOApp {

  val failed: IO[Int] = IO { throw new RuntimeException("boom") }

  val failed2 = IO.raiseError(throw new RuntimeException("boom2"))

  val defaultToInt: IO[Int] = failed2.handleErrorWith {
    case _: RuntimeException => IO.pure(42)
    case other => IO.pure(11)
  }

  val asEither: IO[Either[Throwable, Int]] = failed.attempt

  val redeemed: IO[Int] = failed.redeem(
    _ => 2,
    _ + 1
  )

  def run(args: List[String]): IO[ExitCode] =
    failed2 as ExitCode.Success
}
