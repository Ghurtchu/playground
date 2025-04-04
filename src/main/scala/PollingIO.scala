import PollingIO.PlayerActions.Result
import cats.effect.{IO, IOApp, Sync, Temporal}
import cats.implicits.catsSyntaxApplicativeId

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.util.Random

object PollingIO extends IOApp.Simple {

  type F[A] = cats.effect.IO[A]
  val StatePollingInterval = 200.millis

  object PlayerActions {
    sealed trait Result

    object Result {
      case object Completed extends Result
      case object Pending extends Result
    }
  }

  val onAllBetsAccepted: F[Unit] = IO.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")

  def pending(gameRoundId: String): F[Either[String, PlayerActions.Result]] = {
    for {
      n <- Sync[F].delay(Random.nextInt(10))
      res = if (n == 5) Left("bom") else if (n < 5) Left("lmao") else Right(if (n <= 8) Result.Pending else Result.Completed)
      _ <- IO.println(s"pending generated: $res")
    } yield res
  }


  def start(betAcceptanceTime: FiniteDuration, gameRoundId: String): F[Unit] = {
    def poll(elapsedTime: FiniteDuration): F[Unit] = {
      def keepPolling(logF: F[Unit]): F[Unit] = for {
        _ <- logF
        _ <- Temporal[F].sleep(StatePollingInterval)
        _ <- poll(elapsedTime + StatePollingInterval)
      } yield ()

      if (elapsedTime >= betAcceptanceTime)
        IO.println("bet acceptance time was elapsed, stopped polling")
      else {
          pending(gameRoundId)
          .flatMap {
            case Right(PlayerActions.Result.Completed) =>
              val savedTimeInMillis = (betAcceptanceTime - elapsedTime).toMillis

              onAllBetsAccepted *> IO.println(
                s"All bets were accepted, switching to dealing immediately, saved: $savedTimeInMillis millis",
              )
            case Right(PlayerActions.Result.Pending) =>
              keepPolling(IO.println("Some bets are still pending, retrying..."))
            case Left("boom") =>
              IO.raiseError(new RuntimeException("blow up"))
            case Left(error) =>
              keepPolling(IO.println(s"Got an error: $error, retrying..."))
          }
          .handleErrorWith { exc => keepPolling(IO.println(s"Got an exception: $exc, retrying...")) }
      }
    }

    poll(0.seconds).start.void
  }

  override def run: IO[Unit] =
    for {
      _ <- IO.println("start")
      _ <- start(3.seconds, "game-123")
      _ <- IO.println("[CONTINUE]")
      _ <- IO.sleep(5.seconds)
    } yield ()
}
