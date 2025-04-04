import cats.Functor
import cats.effect.IO
import cats.implicits.{catsSyntaxFunctorTuple2Ops, catsSyntaxIfF, toFunctorOps}


object FunctorEx extends App {

  val f = Functor[Option]
  // f.fmap()

  println{
    Map(1 -> true, 2 -> false).map {
      case (i, i1) => i -> (1 + (if (i1) 1 else 0))
    }
  }

  println {
    Map(1 -> true, 2 -> false).fmap(1 + Option.when(_)(1).getOrElse(0))
  }

  /**
   * Some((5, 5))
   */
  println(Option(5).fproduct(_.toString))

  /**
   * None
   */
  println(Option.empty[String].fproduct(_.toInt))

  Option(true).ifF("true", "false").foreach(println)
  Option.empty[Boolean].ifF("true", "false").foreach(println)

  type Key = String
  val readConfig: Key => Option[Boolean] = _ => Some(true)

  readConfig("a").ifF("it's there", "it's not there").foreach { println }

  Functor[IO].ifF(IO.pure(true))( "it's there", "not there")
  IO.pure(true).ifF( "it's there", "not there")
  val f2: IO[Int] => IO[String] = Functor[IO].lift((i: Int) => i.toString: String)

  val fio = Functor[IO]

  // lift example

  // imagine old java codebase that does:
  type User = String
  trait UserRepo {
    def read(userId: String): User
  }

  val oldUserRepo = new UserRepo {
    override def read(userId: User): User = "User"
  }

  val functionalRepo: IO[String] => IO[User] = Functor[IO].lift(oldUserRepo.read)

  val read: IO[User] = for {
    user <- functionalRepo(IO("1"))
  } yield user

  fio.unzip(IO((1, "2")))

  val (a, b) = Option((1, "2")).unzip
  println(a)
  println(b)

  val (five, string) = Option(5).tupleRight("string").unzip
  println(five)
  println(string)




}
