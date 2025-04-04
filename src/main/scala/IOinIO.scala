import cats.effect.{IO, IOApp}

object IOinIO extends IOApp.Simple {

  val program =
    for {
      _ <- IO.println("start")
      _ <- IO.println("suspending")
      handle <- IO(IO.println("it will be ran at the end"))
      _ <- IO.println("start")
      _ <- handle
    } yield ()

  override def run: IO[Unit] = program
}
