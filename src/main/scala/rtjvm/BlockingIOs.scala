package rtjvm

import cats.effect.{IO, IOApp}
import utils.IO_Ops

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

object BlockingIOs extends IOApp.Simple {

  val someSleeps = for {
    _ <- IO.sleep(1.second).Debug // SEMANTIC BLOCKING - threads are not blocked
    _ <- IO.sleep(1.second).Debug
  } yield ()

  // really blocking IOs
  val aBlockingIO = IO.blocking {
    Thread sleep 1000

    println(s"[${Thread.currentThread().getName}] computed a blocking code")
    42
  } // evaluated on another specific thread for blocking calls

  // yielding
  val iosOnManyThreads = for {
    _ <- IO("first computation").Debug
    _ <- IO.cede // a signal to yield control over the thread, equivalent to IO.shift
    _ <- IO("second").Debug
    _ <- IO.cede
    _ <- IO("third").Debug
  } yield ()

  def testThousandEffectsSwitch() = {
    val ec: ExecutionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(8))
    val thousandCedes = (1 to 1000).map(IO.pure).reduce(_.Debug >> IO.cede >> _.Debug).evalOn(ec)

    thousandCedes
  }

  override val run: IO[Unit] = testThousandEffectsSwitch().void
}
