package rtjvm

import cats.effect.kernel.Outcome
import cats.effect.{Fiber, FiberIO, IO, IOApp}
import utils.IO_Ops

import scala.concurrent.duration.DurationInt

object Fibers extends IOApp.Simple {

  val meaningOfLife: IO[Int] = IO.pure(42)
  val favLang: IO[String] = IO.pure("Scala")

  def sameThreadIOs(): IO[Unit] = for {
    mol <- meaningOfLife.Debug
    lang <- favLang.Debug
  } yield ()

  // introducing Fiber
  // Fiber is like thread, it will run (IO) on some thread managed by CE runtime

  // almost impossible to create fibers manually
  def createFiber: Fiber[IO, Throwable, String] = ???

  // the fiber is not actually started, but the fiber allocation is wrapped in another effect
  val aFiber: IO[Fiber[IO, Throwable, Int]] = meaningOfLife.Debug.start

  def differentThreadIOs() = for {
    _ <- aFiber
    _ <- favLang.Debug
  } yield ()

  // joining a fiber
  def runOnSomeOtherThread[A](io: IO[A]): IO[Outcome[IO, Throwable, A]] = for {
    fib <- io.start
    result <- fib.join // also an effect, which when performed will wait for fiber to terminate
  } yield result // IO[Outcome[IO, Throwable, A]]

  /**
   * IO[ResultType of fib.join]
   * fib.join = IO[A]
   */

  /**
   * possible outcomes:
   *  - success with an IO
   *  - failure with an exception
   *  - cancelled
   */

  val someIOOnAnotherThread = runOnSomeOtherThread(meaningOfLife)

  val someResultFromAnotherThread = someIOOnAnotherThread.flatMap {
    case Outcome.Succeeded(fa) => fa
    case Outcome.Errored(e) => IO pure 0
    case Outcome.Canceled() => IO pure 0
  }

  def throwOnAnotherThread(): IO[Outcome[IO, Throwable, Int]] = for {
    fib <- IO.raiseError[Int](new RuntimeException("no number for you")).start
    result <- fib.join
  } yield result

  def testCancel() = {
    val task: IO[String] = IO("starting").Debug >> IO.sleep(1.second) >> IO("done").Debug
    // onCancel is a "finalizer", allowing you to free up resources in case you get leaks
    val taskWithCancellationHandler = task.onCancel(IO("Sadly, cancelled..").Debug.void)

    for {
      fib <- taskWithCancellationHandler.start // on a separate thread
      _ <- IO.sleep(500.millis) >> IO("cancelling").Debug
      _ <- fib.cancel
      result <- fib.join
    } yield result
  }

  override val run: IO[Unit] =
    testCancel() // IO(Succeeded(IO(42)))
      .Debug.void
}
