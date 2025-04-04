package rtjvm

import cats.effect.{Fiber, IO, IOApp, MonadCancel, Spawn}

object PolymorphicFibers extends IOApp.Simple {

  trait MyGenSpawn[F[_], E] extends MonadCancel[F, E] {
    def start[A](fa: F[A]): F[Fiber[F, Throwable, A]] // creates a fiber wrapped in effect type F
    def never[A]: F[A] // a forever-suspending effect
    def cede: F[Unit] // a "yield" effect
  }

  // Spawn = create fibers for any effect
  trait MySpawn[F[_]] extends MyGenSpawn[F, Throwable] {
    def start[A](fa: F[A]): F[Fiber[F, Throwable, A]] // creates a fiber wrapped in effect type F
    def never[A]: F[A] // a forever-suspending effect
    def cede: F[Unit] // a "yield" effect
  }

  val mol = IO(42)
  val fiber: IO[Fiber[IO, Throwable, Int]] = mol.start

  // pure, map/flatMap, raiseError, uncancelable, start
  val spawnIO = Spawn[IO] // fetch the given / implicit Spawn[IO]

  def ioOnSomeThread[A](io: IO[A]) = for {
    fib <- spawnIO.start(io) // io.start assumes the presence of a Spawn[IO]
    result <- fib.join
  } yield result

  // generalize
  def effectOnSomeThread[F[_], A](fa: F[A])(implicit spawn: Spawn[F]) = ???

  override def run: IO[Unit] = ???
}
