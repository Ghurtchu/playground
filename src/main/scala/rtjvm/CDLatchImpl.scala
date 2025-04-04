package rtjvm

import cats.effect.{IO, Ref}
import cats.effect.kernel.Deferred

import scala.collection.immutable.Queue

object CDLatchImpl {

  abstract class Latch {
    def await: IO[Unit]
    def release: IO[Unit]
  }

  object Latch {
    private type CompletionSignal = Deferred[IO, Unit]

    sealed trait State

    case object Done extends State
    case class Live(remainingCount: Int, signal: Deferred[IO, Unit]) extends State

    def apply(count: Int): IO[Latch] = for {
      signal <- Deferred[IO, Unit]
      state <- Ref[IO].of[State](Live(count, signal))
    } yield new Latch {
      override def await: IO[Unit] = state.get.flatMap {
        case Done => IO.unit
        case Live(_, signal) => signal.get // block here
      }
      override def release: IO[Unit] = state.modify {
        case Done => Done -> IO.unit
        case Live(1, signal) => Done -> signal.complete(()).void
        case Live(n, signal) => Live(n - 1, signal) -> IO.unit
      }.flatten
    }
  }

}
