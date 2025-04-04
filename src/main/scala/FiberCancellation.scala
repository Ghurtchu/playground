import cats.effect.{IO, IOApp}
import utils.IO_Ops

import scala.concurrent.TimeoutException
import scala.concurrent.duration.DurationInt

object FiberCancellation extends IOApp.Simple {

  val program = for {
    fiber <- IO.delay {
      // long task
      Thread sleep 4200

      42
    }.start.Debug
    _ <- IO("computation started").Debug
    _ <- fiber.join.Debug
    _ <- IO("printed after value was received, fiber was joined").Debug
  } yield ()

  val cancelled = for {
    fiber <- IO.delay {
      // long task
      Thread sleep 4200

      42
    }.start.Debug
    _ <- fiber.join.timeout(2.seconds).handleErrorWith {
      case _: TimeoutException => IO.println("timed out, cancelling") *> fiber.cancel
      case _ => IO.println("cancelling anyways") *> fiber.cancel
    }
    _ <- IO.println("cancelled")
  } yield ()

  override def run: IO[Unit] = cancelled
}
