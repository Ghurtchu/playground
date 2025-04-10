package streams

import cats.effect.{IO, IOApp}
import fs2.concurrent.Topic
import cats.instances.all._

import scala.concurrent.duration.{DurationInt, FiniteDuration}

object StreamsAndTopic extends IOApp.Simple {

  /* Topic is basically Kafka :D many consumers + many producers */

  val program = for {
    /* multiple consumers and multiple producers */
    /* 2 x 3 example */
    topic <- Topic[IO, String]
    consumer1 <- topic.subscribe(10)
      .evalMap { str => IO.println(s"Consumer[A] consumed $str") }
      .compile
      .drain
      .start
    consumer2 <- topic.subscribe(10)
      .evalMap { str => IO.println(s"Consumer[B] consumed $str") }
      .compile
      .drain
      .start
    writerStream = topic
      .publish
      .compose[(FiniteDuration, String)] { case (time, string) => fs2.Stream.awakeEvery[IO](time).map(_ => string) }
    writer1 <- writerStream(1.second, "Hello!").compile.drain.start
    writer2 <- writerStream(500.millis, "Good bye!").compile.drain.start
    writer3 <- writerStream(250.millis, "How are you doing?").compile.drain.start
    _ <- IO.never[Unit]
  } yield ()

  override def run: IO[Unit] = program
}
