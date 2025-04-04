package streams

import cats.effect.std.Queue
import cats.effect.{IO, IOApp}

import scala.concurrent.duration.DurationInt

object StreamFromQueue extends IOApp.Simple {

  val run = for {
    _ <- IO.println("start")
    q <- Queue.circularBuffer[IO, String](10)
    stream = fs2.Stream.fromQueueUnterminated[IO, String](q)
      .evalMap { s => IO.pure(s.toUpperCase)
      .flatTap(IO.println) }
    _ <- stream.compile.drain.start
    _ <- q.offer("one")
    _ <- IO.sleep(1.second)
    _ <- q.offer("two")
    _ <- IO.sleep(1.second)
    _ <- q.offer("three")
    _ <- IO.sleep(1.second)
    _ <- IO.println("end")
  } yield ()
}
