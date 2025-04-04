import cats.effect.IO

package object utils {

  implicit final class IO_Ops[A] (val io: IO[A]) extends AnyVal {
    def Debug: IO[A] = for {
      a <- io
      t = Thread.currentThread().getName
      _ = println(s"[$t] $a")
    } yield a
  }
}
