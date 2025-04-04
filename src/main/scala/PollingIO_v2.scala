import cats.Applicative
import cats.effect.{Async, IO, IOApp, Sync}
import cats.syntax.all._
import cats.effect.syntax.all._

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.concurrent.duration.{DurationInt, FiniteDuration}



object PollingIO_v2 extends IOApp.Simple {

  trait PlayerActionsStatusTracker {
    def waitCompleted(gameRoundId: String, until: FiniteDuration): IO[Boolean]
  }

  def pending(): IO[Either[String, String]] = IO.delay {
    val rand = scala.util.Random
    val n = rand.nextInt(10)

    if (n == 1) {
      throw new RuntimeException("booooooom")
    }
    else if (n <= 6) {
      Left("NoData")
    } else if (n == 73) {
      Right("Completed")
    } else {
      Right("Pending")
    }
  }

  object PlayerActionsStatusTracker {
    def of(): IO[PlayerActionsStatusTracker] =
      for {
        _ <- IO.println("creating PlayerActionsStatusTracker service")
      } yield new PlayerActionsStatusTracker {
        private val StatePollingInterval = 200.millis

        def loop(id: String): IO[Boolean] =
          for {
            _ <- IO.sleep(StatePollingInterval)
            result <- pending().flatMap {
                case Right("Completed") => IO.println(s"Received Completed for $id").as(true)
                case Right("Pending") =>
                  IO.println(s"Some actions are still pending, retrying in $StatePollingInterval...") >> loop(id)
                case Left(error) =>
                  IO.println(s"Got an error: $error, retrying in $StatePollingInterval...") >> loop(id)
              }
              .handleErrorWith { exc =>
                IO.println(s"Got an exception: $exc, retrying in $StatePollingInterval...") >> loop(id)
              }
          } yield result

        def waitCompleted(gameRoundId: String, until: FiniteDuration): IO[Boolean] =
          loop(gameRoundId).timeoutTo(until, IO.println(s"tracking timed out after $until").as(false))
      }
  }

  def logic(tracker: PlayerActionsStatusTracker): IO[Unit] = {

    val on = Instant.now().plus(1, ChronoUnit.SECONDS)

    tracker
      .waitCompleted("game-id", 1.seconds)
      .flatMap { completed =>
        if (completed)
          for {
            now <- IO.realTimeInstant
            savedTimeInMillis = ChronoUnit.MILLIS.between(now, on) max 0L
            _ <- IO.println(s"all bets are processed, saved $savedTimeInMillis millis")
            // TODO: here potentially we could call ctx.toSelf(C.StartDealing)
          } yield ()
        else
          IO.println(s"bets are not processed in ${1.seconds}, no optimization is used")
      }
      .start.void
  }

  override val run = for {
    _ <- IO.println("start")
    tracker <- PlayerActionsStatusTracker.of()
    _ <- logic(tracker)
    _ <- IO.println("continued after tracker, it's non-blocking...")
    _ <- IO.sleep(5.seconds)
    _ <- IO.println("end")
  } yield ()

}
