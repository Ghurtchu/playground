import cats.{Applicative, Traverse}
import cats.effect.{IO, IOApp, Ref}
import cats.syntax.applicative._

object ApplicativeEx extends IOApp.Simple {

  val apl: Applicative[Option] = Applicative[Option]

  val f: Int => String = _.toString
  val d: Option[String] = apl.ap(Some(f))(Some(1))

  apl.pure[Int](5) // same as 5.pure[Option]

  val aplIO: Applicative[IO] = Applicative[IO]

  val d2: IO[Unit] = IO.delay[Unit](println("xd")).whenA(5 == 5)

  val d3 = aplIO.unlessA(5 == 6)(IO.println("hell"))

  val d4: IO[List[String]] = aplIO.replicateA(3, IO("hell")).flatTap(IO.println)
  IO("hell").replicateA(3)

  val d5 = aplIO.replicateA_(5, IO.println("xd"))
  IO.println("xd").replicateA_(5)

  import scala.concurrent.Future

  // traverse

  import cats.syntax.all._


  val data = List(1, 2, 3)
  val oli: Option[List[String]] = data.traverse { i => Option(i.toString) }(implicitly[Applicative[Option]])

  // Traverse
  // (F[A], A) => G[F[B]]
  // List[Int] => Option[List[String]]
  val iotraversed: IO[List[String]] = data.traverse { i => IO.pure(i.toString) }.flatTap(IO.println)

  val optTraversed: IO[Option[String]] = Option(1).traverse(i => IO(i.toString))

  // without traverse
  val optMapped: IO[Option[String]] = Option(1) match {
    case Some(value) => IO(Some(value.toString))
    case None => IO(Option.empty[String])
  }

  val dr: IO[List[String]] = List(1, 2, 3).traverse(i => IO.pure(i.toString))

  val dr2: IO[List[Int]] = List(IO(1), IO(2)).sequence

  val dr3: IO[List[Int]] = List(IO(1), IO(2)).parSequence

  val dr4: IO[Unit] = List(IO(1), IO(2)).parSequence_

  val dr5: IO[Unit] = List(IO(1), IO(2)).parSequence.void

  val res6: IO[List[Int]] = List("1", "2").parTraverse(d => IO(d.toInt))

  List(IO(1), IO(2)).parSequence_

  override def run: IO[Unit] =
    d2 *> d3 *> d4.void *> d5 *> iotraversed.void

}
