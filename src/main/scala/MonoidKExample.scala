import cats.MonoidK
import cats.implicits.toFoldableOps


object MonoidKExample extends App {

  val monoidK: MonoidK[List] = implicitly[MonoidK[List]]

  val data = List(
    (1 to 10).toList, // List(1, 2 .. 10)
    (11 to 20).toList, // ..
    (21 to 30).toList // List(21, 22 .. 30)
  )

  println(MonoidK[List].combineAllK[Int](data))

  /**
   * zips lists together
   */
  println(data.combineAll)

  println(monoidK.empty[List[String]])

  /**
   * combines but in reversed order
   */
  println(monoidK.reverse.combineAllK(data))

  /**
   * combines and then reverses each element (list)
   */
  println(monoidK.combineAllK(data).reverse)

  /**
   * prints alphabet 3 times
   */
  println(monoidK.combineNK(('a' to 'z').map(_.toString).toList, 3))

  /**
   * List(1, 2, 2, 1)
   */
  println(monoidK.combineK(List(1, 2), List(2, 1)))

  println(monoidK.combineAllOptionK(
    List(
      (1 to 10).toList,
      (11 to 20).toList
    )
  ))

  println(monoidK.combineAllOptionK(
    List(
      (1 to 10).toList,
      Nil
    )
  ))

  println(monoidK.combineAllOptionK(
    List(
      Nil,
      Nil
    )
  ))

  /**
   * None
   */
  println(monoidK.combineAllOptionK(
    List.empty
  ))

  val d = List(
    (1 to 10).toList,
    (11 to 20).toList
  )

  println(d.combineAll)
  println(d.flatten)

}
