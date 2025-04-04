import cats.effect.{IO, IOApp, Ref}

import scala.concurrent.duration.DurationInt

object Main extends IOApp.Simple {

  sealed trait HealthCheckResponse

  case object Alive extends HealthCheckResponse
  case object Dead extends HealthCheckResponse

  case class Backends(servers: Set[String]) {
    def current: String = servers.head
    def drop(server: String): Backends = copy(servers - server)
    def add(server: String): Backends = copy(servers + server)
  }

  val backends = Ref.of[IO, Backends](Backends(Set("1", "2", "3")))

  def program(backends: Ref[IO, Backends]): IO[Unit] =
    (for {
      _    <- IO(println("sending request to server..."))
      _    <- IO(println("Request was sent"))
      curr <- backends.get.map(_.current)
      _    <- backends.get.map(println)
      _    <- IO.sleep(3.seconds)
      resp <- IO {
        Thread sleep 6000
        if(scala.util.Random.nextBoolean()) Alive else Dead
      }.timeout(5.seconds)
        .recover(_ => Dead)
      _    <- IO(println(resp))
      _    <- if (resp == Alive) backends.update(_.add(curr)) else backends.update(_.drop(curr))
      _    <- IO.sleep(5.seconds)
      _    <- IO.delay(println("done"))
    } yield ()).foreverM

  def voidExample =
    for {
      _ <- IO.println("oe")
    } yield ()

  def voidVoid = IO.println("oe")

  override def run: IO[Unit] =
    voidVoid


}
