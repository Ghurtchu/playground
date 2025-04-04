import cats.data.OptionT
import cats.effect.{IO, IOApp, Ref}
import cats.syntax.all._

object RefEx extends IOApp.Simple {


  override def run: IO[Unit] =
    for {
      stubs <- Ref.of[IO, Map[String, String]](Map.empty)
      service <- IO.pure(new StubbingService(stubs))
      _ <- service.put("1", "xd")
      _ <- service.put("2", "yd")
      _ <- service.put("1", "xd")
      _ <- service.put("1", "xd")
      maybeVal <- service.getLightning("1").value
      _ <- IO.println(maybeVal)
      _ <- service.currentState
    } yield ()


}

class StubbingService(stubs: Ref[IO, Map[String, String]]) {

  def put(id: String, v: String): IO[Unit] =
    stubs.update(_ + (id -> v))

  private def getAndDrop(id: String): OptionT[IO, String] = OptionT {
    stubs.modify(map => (map - id, map.get(id)))
  }

  def getLightning(id: String): OptionT[IO, String] =
    getAndDrop(id) collect { case "xd" => "xd" }

  def currentState: IO[Unit] = stubs.get.flatTap(IO.println).void

}
