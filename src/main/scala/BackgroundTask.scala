import cats.effect.{ExitCode, IO, IOApp, Resource, ResourceApp}
import cats.implicits.catsSyntaxFlatMapOps

import scala.concurrent.duration.DurationInt

object BackgroundTask extends ResourceApp {

  override def run(args: List[String]): Resource[IO, ExitCode] =
    for {
      _ <- Resource.eval(IO.println("start"))
      ab = ExitCode.Success
      a <- (for {
        _ <- IO.sleep(3.seconds)
        // TODO: improve mechanics, eg store timestamps and drop the oldest ones
        _ <- IO.println("go")
      } yield ()).foreverM.background
      _ <- Resource.eval(IO.sleep(30.seconds))
    } yield ab
}