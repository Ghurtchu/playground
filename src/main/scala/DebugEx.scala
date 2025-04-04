import cats.effect.{IO, IOApp}
import utils.IO_Ops

object DebugEx extends IOApp.Simple {

  implicit class IODebugOps[A](ioa: IO[A]) {
    def inspect: IO[Unit] =
      for {
        a <- ioa.onError(IO.println)
        _ <- IO.println(a)
      } yield ()
  }

  val program = for {
    _ <- IO.delay { 5 }.inspect
    _ <- IO.delay { 4 }.inspect
    _ <- IO.delay { println("here") }
  } yield ()

  // to debug in debugger
  /**
   * to debug in IJ debugger, use:
   * expr.unsafeRunsync()(cats.effect.unsafe.implicits.global)
   */
  /

  override def run: IO[Unit] = program
}
