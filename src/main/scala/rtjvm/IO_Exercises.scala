package rtjvm

import cats.effect.{ExitCode, IO, IOApp}

object IO_Exercises extends IOApp {

  def sequenceTakeLast[A, B](left: IO[A], right: IO[B]): IO[B] =
    for {
      _ <- left
      r <- right
    } yield r

  def sequenceTakeFirst[A, B](left: IO[A], right: IO[B]): IO[A] =
    for {
      l <- left
      _ <- right
    } yield l

  def forever[A](io: IO[A]): IO[Unit] =
    for {
      _ <- io
      _ <- forever(io)
    } yield ()

  def convert[A, B](io: IO[A], b: B): IO[B] = io as b

  def asUnit[A](io: IO[A]): IO[Unit] = io.void

  def sumIO(n: Int): IO[Int] =
    if (n <= 0) IO.pure(n)
    else sumIO(n - 1).map(_ + n)

  val io = IO(1) *> IO(2)

  val forever = io.foreverM

  def run(args: List[String]) =
    sumIO(5).flatMap(IO.println) as ExitCode.Success
}
