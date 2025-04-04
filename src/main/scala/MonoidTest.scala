import MonoidTest.DragonTigerBettingStats.Stat
import cats.kernel.Monoid

object MonoidTest extends App {

  final case class DragonTigerBettingStats(
                                            stats: Map[DragonTigerBettingStats.Stat.Spot, DragonTigerBettingStats.Stat] = Map(
                                              DragonTigerBettingStats.Stat.Dragon -> DragonTigerBettingStats.Stat.Empty,
                                              DragonTigerBettingStats.Stat.Tiger -> DragonTigerBettingStats.Stat.Empty,
                                              DragonTigerBettingStats.Stat.Tie -> DragonTigerBettingStats.Stat.Empty,
                                            ),
                                            watchers: Int = 0,
                                            bettors: Int = 0,
                                          )

  object DragonTigerBettingStats {

    val Empty: DragonTigerBettingStats = DragonTigerBettingStats()

    final case class Stat(amount: BigDecimal = 0, players: Int = 0, percent: Int = 0)

    object Stat {

      val Empty: Stat = Stat(0, 0, 0)

      sealed trait BetSpot

      object BetSpot {
        case object Dragon extends BetSpot
        case object Tiger extends BetSpot
        case object Tie extends BetSpot
        case object SuitedTie extends BetSpot
      }

      sealed trait Spot { def betSpot: BetSpot }

      object Spot {
        def fromBetSpot: BetSpot => Spot = {
          case BetSpot.Dragon => Dragon
          case BetSpot.Tiger => Tiger
          case BetSpot.Tie | BetSpot.SuitedTie => Tie
        }
      }

      case object Dragon extends Spot { def betSpot = BetSpot.Dragon }
      case object Tiger extends Spot { def betSpot = BetSpot.Tiger }
      case object Tie extends Spot { def betSpot = BetSpot.Tie }
    }
  }

  implicit val SpotToStatMonoid: Monoid[Map[Stat.Spot, Stat]] = new Monoid[Map[Stat.Spot, Stat]] {
    override def empty: Map[Stat.Spot, Stat] = Map.empty

    // example:
    // x=Map(Tie -> Stat(5, 1), Tiger -> Stat(2, 1))
    // y=Map(Tiger -> Stat(5, 1))
    // should after monoid.combine it should become: Map(Tie -> Stat(5, 1), Tiger -> Stat(7, 1))

    override def combine(x: Map[Stat.Spot, Stat], y: Map[Stat.Spot, Stat]): Map[Stat.Spot, Stat] =
      (x.toList ::: y.toList)
        .groupBy { case (k, _) => k }
        .map { case (k, v) => (k, v.map { case (_, stat) => stat }.reduce(StatMonoid.combine)) }
  }

  implicit val DragonTigerBettingStatsMonoid: Monoid[DragonTigerBettingStats] = new Monoid[DragonTigerBettingStats] {
    def empty: DragonTigerBettingStats = DragonTigerBettingStats.Empty
    def combine(x: DragonTigerBettingStats, y: DragonTigerBettingStats): DragonTigerBettingStats =
      DragonTigerBettingStats(
        stats = SpotToStatMonoid.combine(x.stats, y.stats),
        watchers = x.watchers + y.watchers,
        bettors = x.bettors + y.bettors,
      )
  }

  implicit val StatMonoid: Monoid[Stat] = new Monoid[Stat] {
    override def empty: Stat = Stat()
    override def combine(x: Stat, y: Stat): Stat = Stat(
      amount = x.amount + y.amount,
      players = x.players + y.players,
    ) // ignore "percent" field, it will be calculated before broadcasting, it's not necessary to be here
  }

  val X = DragonTigerBettingStats(
    stats = Map(Stat.Tie -> Stat(amount = 2, players = 1), Stat.Tiger -> Stat(amount = 5, players = 2)),
    bettors = 3,
    watchers = 2,
  )

  val Y = DragonTigerBettingStats(
    stats = Map(Stat.Tie -> Stat(amount = 3, players = 1)),
    bettors = 1,
    watchers = -1,
  )

  val combined = DragonTigerBettingStatsMonoid.combine(
    x = X,
    y = Y,
  )

  println(combined)

  val empty = DragonTigerBettingStats()
  val first = DragonTigerBettingStats(stats = Map(Stat.Tiger -> Stat(amount = 1, players = 1)), watchers = 0, bettors = 1)

  println(DragonTigerBettingStatsMonoid.combine(x = empty, y = first))

}
