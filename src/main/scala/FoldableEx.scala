import cats.effect.IO
import cats.{Foldable, Invariant}
import cats.syntax.all._

object FoldableEx extends App {

  val lf = Foldable[List]

  // maps each and then folds
  val res = List[Int](1, 2, 3).foldMap(_.toString)
  println(res)

  println {
    (1 to 10).toList.foldMap { n =>
      (n * 2).toString
    }
  }

  println {
    List (
      List (1, 2, 3),
      List (3, 2, 1)
    ).map(_.foldMap(_.toString))
  }

  def parseInt(int: String): Option[Int] = scala.util.Try {
    int.toInt
  }.toOption

  val textNums = List("1", "2", "3")

  println {
    textNums.traverse(parseInt)
  }

}
