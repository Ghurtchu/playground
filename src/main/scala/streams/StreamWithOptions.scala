package streams

import cats.effect.IOApp
import cats.effect.std.Queue
import cats.effect.IO
import cats.effect.kernel.Outcome

import scala.concurrent.duration.DurationInt

object StreamWithOptions extends IOApp.Simple {

  val program = for {
    queue <- Queue.circularBuffer[IO, Option[String]](5)
    fiber <- fs2.Stream.fromQueueNoneTerminated(queue, 10).evalMap { consumed =>
      IO.println(s"consumed event: $consumed")
    }.onFinalize(IO.println("stopping stream")).compile.drain.start
    _ <- queue.offer(Some("1")) >> IO.sleep(500.millis)
    _ <- queue.offer(Some("2")) >> IO.sleep(500.millis)
    _ <- queue.offer(Some("3")) >> IO.sleep(500.millis)
    _ <- queue.offer(None) >> IO.sleep(500.millis)
    outcome <- fiber.join
    _ <- outcome match {
      case Outcome.Succeeded(fa) => IO.println("succeeded") // here
      case Outcome.Errored(e) => IO.println("errored")
      case Outcome.Canceled() => IO.println("canceled")
    }
    _ <- IO.println("end")
  } yield ()

  override val run = program

}
