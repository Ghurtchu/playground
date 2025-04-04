package rtjvm

import cats.effect.{ExitCode, IO, IOApp}

import scala.io.StdIn

object IO_Apps {
  val program: IO[Unit] = for {
      txt <- IO.delay(StdIn.readLine())
        _ <- IO.println(txt)
  } yield ()
}

object FirstCEApp extends IOApp {

  import IO_Apps._

  override def run(args: List[String]): IO[ExitCode] =
    program as ExitCode.Success
}

object MySimpleApp extends IOApp.Simple {

  override def run: IO[Unit] = IO_Apps.program
}
