import cats.data.EitherT
import cats.effect.{IO, IOApp}
import cats.implicits.catsSyntaxTuple2Semigroupal

object CodeSample extends IOApp.Simple {

  type F[A] = cats.effect.IO[A]

  def mayGenerateMultipliers: IO[Either[String, Option[Int]]] =
    IO.pure { Right { Some { 5 } } }

  def mayGenerateMultipliersFailed: IO[Either[String, Option[Int]]] =
    IO.pure(Right(None))

  val program: F[Either[String, Unit]] =
    (for {
      m <- EitherT[F, String, Option[Int]](mayGenerateMultipliers)
      _ = println("1")
      opt: Option[String] = Some("4")
      _ <- (opt, m).traverseN {
        case (a, b) =>
          EitherT.right[String] {
            IO.println(s"executed, both present: $a, $b")
          }
      }
      _ = println("2")
    } yield ()).value

  override val run: IO[Unit] = program.void

}
