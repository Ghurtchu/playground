import cats.effect.{IO, IOApp}

import java.io.{BufferedReader, File, FileReader}

object BracketPattern extends IOApp.Simple {

  val acquire: String => IO[BufferedReader] = path => IO.delay {
    new BufferedReader(new FileReader(new File(path)))
  }

  val release: BufferedReader => IO[Unit] = fileReader => IO.delay(fileReader.close())

  val use: BufferedReader => IO[Unit] = reader =>
    for {
      _ <- IO.delay { reader.lines().forEach(println) }
    } yield ()

  val managed = acquire("src/main/scala/BracketPattern.scala").bracket(use)(release)

  val program = for {
    _ <- managed
  } yield ()

  override val run = program

}
