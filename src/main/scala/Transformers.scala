import cats.data.OptionT
import cats.effect.{IO, IOApp}

object Transformers extends IOApp.Simple {


  override def run: IO[Unit] =
    second.value.void

  val opt1Some: OptionT[IO, Int] = OptionT {
    IO.delay { Option(1) }
  }

  val opt1None: OptionT[IO, Int] = OptionT {
    IO.delay { Option.empty[Int] }
  }

  val opt2None: OptionT[IO, Int] = OptionT {
    IO.delay { Option.empty[Int] }
  }

  val opt2Some: OptionT[IO, Int] = OptionT {
    IO.delay { Option(5) }
  }

  val first =
    opt1Some.semiflatTap(n => IO.delay(println(s"first ${n}")))
      .orElse(opt2None.semiflatTap(n => IO.delay(println(s"second ${n}"))))

  val second =
    opt1None.semiflatTap(n => IO.delay(println(s"first ${n}")))
      .orElse(opt2Some.semiflatTap(n => IO.delay(println(s"second ${n}"))))

}
