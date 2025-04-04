package rtjvm

import cats.{Applicative, Monad}
import cats.effect.{IO, IOApp, MonadCancel, Poll}

object PolymorphicCancellation extends IOApp.Simple {

  trait MyApplicativeError[F[_], E] extends Applicative[F] {
    def raiseError[A](error: E): F[A]
    def handleErrorWith[A](fa: F[A])(f: E => F[A]): F[A]
  }

  trait MyMonadError[F[_], E] extends MyApplicativeError[F, E] with Monad[F]

  trait MyPoll[F[_]] {
    def apply[A](fa: F[A]): F[A]
  }

  // MonadCancel
  trait MyMonadCancel[F[_], E] extends MyMonadError[F, E] {
    def cancelled: F[Unit]
    def uncancelable[A](poll: Poll[F] => F[A]): F[A]
  }

  // monadCancel for IO
  val monadCancel: MonadCancel[IO, Throwable] = MonadCancel[IO]

  // we can create values
  val molIO: IO[Int] = monadCancel.pure(42)
  val ambitiousMolIO: IO[Int] = monadCancel.map(molIO)(_ * 10)

  val mustCompute = monadCancel.uncancelable { _ =>
    for {
      _ <- monadCancel.pure("once started, can't go back")
      res <- monadCancel.pure(56)
    } yield res
  }

  import cats.syntax.all._

  // can generalize code
  def mustComputeGeneral[F[_]: Monad, E](implicit mc: MonadCancel[F, E]): F[Int] = mc.uncancelable { _ =>
    for {
      _ <- mc.pure("once started, can't go back")
      res <- mc.pure(56)
    } yield res
  }

  val mustCompute_v2 = mustComputeGeneral[IO, Throwable]

  // allow cancellation listeners
  val mustComputeWithListener = mustCompute.onCancel(IO("I'm being canceled").void)

  override def run: IO[Unit] = ???
}
