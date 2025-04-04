package rtjvm

import cats.Parallel
import cats.effect.{IO, IOApp}
import utils.IO_Ops

object IO_Parallelism extends IOApp.Simple {

  // IOs are usually sequential

  val anisIO = IO.pure(s"[${Thread.currentThread().getName}] Ani")
  val kamranIO = IO.pure(s"[${Thread.currentThread().getName}] Kamran")

  val composedIO = for {
      ani <- anisIO
      kamran <- kamranIO
    } yield s"$ani and $kamran love Rock the JVM"

  // debug extension method
  val meaningOfLife: IO[Int] = IO.delay(42)
  val favLang: IO[String] = IO.delay("Scala")

  import cats.syntax.apply._
  val goalInLife = (meaningOfLife.Debug, favLang.Debug).mapN { (num, str) =>
    s"my goal in life is $num and $str"
  }

  // parallelism on IOs
  // convert a sequential IO to parallel IO
  val parIO1: IO.Par[Int] = Parallel[IO].parallel(meaningOfLife.Debug)
  val parIO2: IO.Par[String] = Parallel[IO].parallel(favLang.Debug)

  val goalInLifeParallel = (parIO1, parIO2).mapN{ (num, str) =>
    s"my goal in life is $num and $str"
  }
  // turn back to sequential
  val goalInLife_v2: IO[String] = Parallel[IO].sequential(goalInLifeParallel)

  // shorthand:
  import cats.syntax.parallel._

  // runs left on thread 1
  // runs right on thread 2
  // combines result
  val goalInLife_v3: IO[String] = (meaningOfLife.Debug, favLang.Debug).parMapN { (num, str) =>
      s"my goal in life is $num and $str"
    }

  // what if one of the IO fails?
  val aFailure: IO[String] = IO.raiseError(new RuntimeException("I can't do this"))

  // compose success + failure
  val parallelWithFailure: IO[String] = (meaningOfLife.Debug, aFailure.Debug)
    .parMapN(_ + _)
    .handleError {
      case _: RuntimeException => "failed"
    }

  val delayedParMapN = (
    IO(Thread sleep 1000) >> aFailure.Debug,
    meaningOfLife.Debug
  ).parMapN { (num, str) =>
    s"my goal in life is $num and $str"
  }

  val run: IO[Unit] = delayedParMapN.Debug.void
}
