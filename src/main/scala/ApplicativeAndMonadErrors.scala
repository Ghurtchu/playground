import cats.ApplicativeError
import cats.effect.{IO, IOApp}
import cats.instances.all._
import cats.implicits._
import cats._
import cats.syntax.all._

object ApplicativeAndMonadErrors extends IOApp.Simple {

  def safeDivide(x: Int, y: Int): Either[String, Int] =
    if (y == 0) Left("Can't divide by zero") else Right(x / y)

  def safeDivideAp[F[_]](x: Int, y: Int)(implicit ae: ApplicativeError[F, String]): F[Int] = {
    if (y == 0) ae.raiseError("Can't divide by zero")
    else ae.pure(x / y)
  }

  def safeDivideIO(x: Int, y: Int): IO[Int] = {
    if (x == 0) IO.raiseError[Int](new RuntimeException("Can't divide by zero"))
    else IO.pure { x / y }
  }

  override def run: IO[Unit] = safeDivideIO(4, 0).flatTap(IO.println).void
}
