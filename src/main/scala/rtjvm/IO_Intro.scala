package rtjvm

import cats.effect._
import cats.syntax.all._

object IO_Intro extends IOApp {

  val io: IO[Int] = IO.pure(42)
  val delayed: IO[Int] = IO.delay({println("I am summing two numbers"); 42})

  val delayed2 = IO {
    println("doing something")

    54
  }

  val program = delayed2.flatMap(n => delayed.map(m => n + m))

  def out: Any => IO[Unit] = IO.println

  val n: IO[Int] = IO.delay(scala.util.Random.nextInt(10))

  val mapn: IO[Int] = (n, n, n).mapN(_ + _ + _)

  val p1 = program.flatMap(out).as(ExitCode.Success)
  val p2 = mapn.flatMap(out)

  override def run(args: List[String]): IO[ExitCode] =
    p2 as ExitCode.Success
}
