package rtjvm

import cats.effect.std.CountDownLatch
import cats.effect.{IO, IOApp, Resource}
import cats.syntax.all._
import utils.IO_Ops

import java.io.{File, FileWriter}
import scala.concurrent.duration._
import scala.io.Source
import scala.util.Random

object CountdownLatches extends IOApp.Simple {

  def announcer(latch: CountDownLatch[IO]): IO[Unit] =
    for {
      _ <- IO("Starting race shortly...").Debug >> IO.sleep(2.seconds)
      _ <- IO("5...").Debug >> IO.sleep(1.seconds)
      _ <- latch.release
      _ <- IO("4...").Debug >> IO.sleep(1.seconds)
      _ <- latch.release
      _ <- IO("3...").Debug >> IO.sleep(1.seconds)
      _ <- latch.release
      _ <- IO("2...").Debug >> IO.sleep(1.seconds)
      _ <- latch.release
      _ <- IO("1...").Debug >> IO.sleep(1.seconds)
      _ <- latch.release
      _ <- IO("GO GO GO").Debug
    } yield ()

  def createRunner(id: Int, latch: CountDownLatch[IO]): IO[Unit] = for {
    _ <- IO(s"[runner $id] waiting for signal...").Debug
    _ <- latch.await // block this fiber until the count reaches 0
    _ <- IO(s"[runner $id] RUNNING!").Debug
  } yield ()

  def sprint(): IO[Unit] = for {
    latch <- CountDownLatch[IO](5)
    announceFib <- announcer(latch).start
    _ <- (1 to 10).toList.parTraverse { id => createRunner(id, latch) }
    _ <- announceFib.join
  } yield ()

  /**
   * Exercise: simulate a file downloader on multiple threads
   */

  object FileServer {

    val fileChunkList = Array(
      "I love Scala.",
      "Cats Effect seems quite fun.",
      "Never would I have thought I would do low-level concurrency WITH pure FP.",
    )

    def getNumChunks: IO[Int] = IO(fileChunkList.length)
    def getFileChunk(n: Int): IO[String] = IO(fileChunkList(n))
  }

  def writeToFile(path: String, contents: String): IO[Unit] = {
    val fileResource = Resource.make(IO(new FileWriter(new File(path)))) { writer =>
      IO(writer.close())
    }

    fileResource.use { writer => IO(writer.write(contents)) }
  }

  def appendFileContents(fromPath: String, toPath: String): IO[Unit] = {
    val compositeResource = for {
      reader <- Resource.make(IO(Source.fromFile(fromPath)))(source => IO(source.close()))
      writer <- Resource.make(IO(new FileWriter(new File(toPath), true)))(writer => IO(writer.close()))
    } yield (reader, writer)

    compositeResource.use { case (reader, writer) =>
      IO(reader.getLines().foreach(writer.write))
    }
  }

  import cats.syntax.all._

  def createFileDownloaderTask(id: Int, latch: CountDownLatch[IO], filename: String, destFolder: String): IO[Unit] = for {
    _ <- IO(s"[task $id] downloading chunk...").Debug
    _ <- IO.sleep((Random.nextDouble() * 1000).toInt.millis)
    chunk <- FileServer.getFileChunk(id)
    _ <- writeToFile(s"$destFolder/$filename.part$id", chunk)
    _ <- IO(s"[task $id] chunk download complete").Debug
    _ <- latch.release
  } yield ()

  def downloadFile(filename: String, destFolder: String): IO[Unit] = for {
    n <- FileServer.getNumChunks
    latch <- CountDownLatch[IO](n)
    _ <- IO(s"Download started on $n fibers").Debug
    _ <- (0 until n).toList.parTraverse(id => createFileDownloaderTask(id, latch, filename, destFolder))
    _ <- latch.await
    _ <- (0 until n)
      .toList
      .traverse(id => appendFileContents(s"$destFolder/$filename.part$id", s"$destFolder/$filename"))
  } yield ()

  override def run: IO[Unit] = downloadFile("myScalafile.txt", "ce-ref-practice/src/main/resources")
}
