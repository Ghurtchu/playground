package rtjvm

import cats.effect.kernel.Deferred
import cats.effect.{IO, IOApp, Ref}
import cats.syntax.parallel._

import scala.collection.immutable.Queue
import scala.concurrent.duration.DurationInt
import scala.util.Random

abstract class MyMutex {
  def acquire: IO[Unit]
  def release: IO[Unit]
}

object MyMutex {
  private type CompletionSignal = Deferred[IO, Unit]
  final case class State(locked: Boolean, waiting: Queue[CompletionSignal])

  private val Unlocked = State(locked = false, Queue.empty)

  def createMutexWithCancellation(state: Ref[IO, State]): MyMutex = new MyMutex {
    override def acquire: IO[Unit] = IO.uncancelable { poll =>
      Deferred[IO, Unit].flatMap { signal =>

        val cleanup = state.modify {
          case State(locked, queue) =>
            val newQueue = queue.filterNot(_ eq signal)

            State(locked, newQueue) -> release
        }.flatten

        state.modify { // IO[IO[Unit]]
          case State(false, _) => State(locked = true, Queue()) -> IO.unit
          case State(true, queue) => State(locked = true, queue.enqueue(signal)) -> poll(signal.get).onCancel(cleanup)
        }.flatten
      }
    }

    override def release: IO[Unit] = state.modify {
      case State(false, _) => Unlocked -> IO.unit
      case State(true, queue) =>
        if (queue.isEmpty)
          Unlocked -> IO.unit
        else {
          val (signal, rest) = queue.dequeue

          State(locked = true, rest) -> signal.complete(()).void
        }
    }.flatten
  }

  def create: IO[MyMutex] = Ref[IO].of(Unlocked).map(createMutexWithCancellation)
}

object MutexPlayground extends IOApp.Simple {

  import syntax.IODebugOps

  def criticalTask: IO[Int] =
    IO.sleep(1.second) >> IO(Random.nextInt(100))

  def lockingTask(id: Int, mutex: MyMutex): IO[Int] = for {
    _ <- IO(s"[task $id] waiting for permission...").printThread
    _ <- mutex.acquire // blocks if the mutex has been acquired by some other fiber
    _ <- IO(s"[task $id] working...").printThread
    // --------------- critical section -------------
    res <- criticalTask
    // --------------- critical section -------------
    _ <- IO(s"[task $id] got result: $res").printThread
    _ <- mutex.release // critical task release, other fiber can start executing the critical task
    _ <- IO(s"[task $id] lock removed.").printThread
  } yield res

  def demoLockingTasks: IO[List[Int]] = for {
    mutex <- MyMutex.create
    results <- (1 to 10).toList.parTraverse { id =>
      lockingTask(id, mutex)
    }
  } yield results

  // only one task proceeds at a time
  override def run: IO[Unit] = demoLockingTasks.void
}

object syntax {
  implicit final class IODebugOps[A](val io: IO[A]) extends AnyVal {
    def printThread: IO[A] = for {
      a <- io
      t = Thread.currentThread().getName
      _ = println(s"[$t] $a")
    } yield a
  }
}
