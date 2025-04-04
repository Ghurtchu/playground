package typelevel_tutorials

import cats.effect.{IO, IOApp}
import cats.effect.Resource

import java.io.{File, FileInputStream, FileOutputStream, InputStream, OutputStream}

object CopyingFiles extends IOApp.Simple {

  def copy(original: File, destination: File): IO[Long] = ???

  def inputStream(f: File): Resource[IO, FileInputStream] =
    Resource.make {
      IO.blocking(new FileInputStream(f)) // build
    } { inStream =>
      IO.blocking(inStream.close()).handleErrorWith(_ => IO.unit) // release
    }

  def inputStreamShorter(f: File): Resource[IO, FileInputStream] =
    Resource.fromAutoCloseable { IO(new FileInputStream(f)) } // build } { inStream =>

  def outputStream(f: File): Resource[IO, FileOutputStream] =
    Resource.make {
      IO.blocking(new FileOutputStream(f)) // build
    } { outStream =>
      IO.blocking(outStream.close()).handleErrorWith(_ => IO.unit) // release
    }

  def inAndOutStreams(in: File, out: File): Resource[IO, (InputStream, OutputStream)] =
    for {
      inStream <- inputStream(in)
      outStream <- outputStream(out)
    } yield inStream -> outStream

  // transfer does the real work
  def transfer1(origin: InputStream, destination: OutputStream): IO[Long] = ???

  def copy1(origin: File, destination: File): IO[Long] =
    inAndOutStreams(origin, destination).use { case (in, out) =>
      transfer(in, out)
    }

  import cats.effect.IO
  import cats.syntax.all._
  import java.io._

  def transfer(origin: InputStream, destination: OutputStream): IO[Long] = ???

  def copyWithBracket(origin: File, destination: File): IO[Long] = {
    val inIO: IO[InputStream] = IO(new FileInputStream(origin))
    val outIO: IO[OutputStream] = IO(new FileOutputStream(destination))

    (inIO, outIO)
      .tupled
      .bracket { case (in, out) =>
        transfer(in, out)
      } { case (in, out) =>
        (IO(in.close()), IO(out.close()))
        .tupled
        .void
        .handleErrorWith(_ => IO.unit)
      }
  }

  override def run: IO[Unit] = ???
}
