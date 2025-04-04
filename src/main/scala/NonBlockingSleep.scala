import cats.effect.{IO, IOApp}

import scala.concurrent.duration.DurationInt

object NonBlockingSleep extends IOApp.Simple {

  val program = {
    for {
      _ <- IO.println("start")
      _ <- (IO.sleep(2.second) >> IO.println("stopping stream after 2 seconds")).start
      _ <- IO.println("continuing with the rest of the code...")
      _ <- IO.sleep(4.seconds)
      _ <- IO.println("4 second task was done")
    } yield ()
  }


  override val run = program

}
