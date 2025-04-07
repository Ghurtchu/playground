package streams

import cats.effect.std.Queue
import cats.effect.{IO, IOApp}
import cats.implicits.toTraverseOps

import scala.concurrent.duration.DurationInt

object StreamFromQueue2 extends IOApp.Simple {

  case class Bet(spot: String, amount: Int)

  case class Player(id: String, queue: Queue[IO, Bet]) {
    def placeBet(bet: Bet): IO[Unit] = queue.offer(bet)
  }

  def playerWithBet(players: Seq[Player]): (Player, Bet) = {
    val rand = scala.util.Random.nextInt(10)
    val spot = scala.util.Random.shuffle(Set("a", "b", "c")).head
    val amount = scala.util.Random.shuffle(Set(1, 2, 3)).head

    players(rand) -> Bet(spot, amount)
  }

  val program = for {
    queue <- Queue.circularBuffer[IO, Bet](10)
    players = (1 to 10).map { i => Player(i.toString, queue) }
    stream = fs2.Stream.fromQueueUnterminated(queue).evalTap { bet =>
      IO.println(s"new bet appeared: $bet")
    }
    _ <- stream.compile.drain.start
    _ <- {
      for {
        _ <- IO.sleep(scala.util.Random.shuffle(Set(1, 2, 3, 4, 5)).head.seconds)
        _ <- {
          val n = scala.util.Random.nextInt(5)
          val actions = (1 to n).map { _ => playerWithBet(players) }.toList

          actions.traverse { case (player, bet) =>
            IO.println(s"$player is writing to queue") >> player.placeBet(bet)
          }
        }
      } yield ()
    }.foreverM.start.void
    _ <- IO.never[Unit]
  } yield ()

  override def run: IO[Unit] = program
}
