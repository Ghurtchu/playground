import cats.data.OptionT
import cats.effect.{Deferred, IO, IOApp}

import scala.concurrent.duration.DurationInt

object OptionTExample extends IOApp.Simple {

  val newProgram = for {
    task <- Deferred[IO, Unit]
    _ <- (task.complete(()) >> IO.println("job")).start
    _ <- task.complete().void.onError(IO.println)
  } yield ()

  val mainProgram = for {
    _ <- IO.println("start")
    _ <- newProgram
    _ <- IO.println("after newProgram")
    _ <- IO.sleep(3000.millis)
    _ <- IO.println("finish")
  } yield ()

  override def run: IO[Unit] = mainProgram
}
