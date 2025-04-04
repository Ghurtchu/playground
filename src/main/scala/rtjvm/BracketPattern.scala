package rtjvm

import cats.effect.{IO, IOApp, Resource}
import utils.IO_Ops

import java.io.{File, FileReader}
import java.util.Scanner
import scala.concurrent.duration.DurationInt

object BracketPattern extends IOApp.Simple {

  // we will arrive at Resource
  // use-case: manage a connection lifecycle
  class Connection(url: String) {
    def open(): IO[String] = IO(s"opening connection to $url").Debug

    def close(): IO[String] = IO(s"closing connection to $url").Debug
  }

  val asyncFetchUrl = for {
    fib <- (new Connection("rockthejvm.com").open() *> IO.sleep(Int.MaxValue.seconds)).start
    _ <- IO.sleep(1.second) *> fib.cancel
  } yield ()

  val correctAsyncFetchUrl = for {
    conn <- IO(new Connection("rockthejvm.com"))
    fib <- (conn.open() *> IO.sleep(Int.MaxValue.seconds)).onCancel(conn.close().void).start
    _ <- IO.sleep(1.second) *> IO("cancelling fiber").Debug *> fib.cancel
  } yield ()

  // Bracket pattern - resource acquisition + release
  val bracketFetchUrl = IO(new Connection("rockthejvm.com"))
    .bracket { conn =>
      conn.open() *> IO.sleep(Int.MaxValue.seconds)
    } { conn =>
      conn.close().void
    }

  val bracketProgram = for {
    fib <- bracketFetchUrl.start
    _ <- IO.sleep(1.second) *> fib.cancel
  } yield ()

  /**
   * bracket pattern: someIO.bracket(useResourceCallback)(releaseResourceCallback)
   *
   * useResourceCallback can be canceled by some other fiber and releaseResourceCallback will be always executed
   *
   * looks like try-catch for managing resources
   */

  // exercise: open a file with text, print all lines every 100 millis and then close the file
  // - open a scanner
  // - read the file line by line, every 100 millis (sleep) and print
  // - if everything was successful close the scanner
  // - if smth went wrong and it was canceled, close it anyways
  def openFileScanner(path: String): IO[Scanner] = IO.delay {
    new Scanner(new FileReader(new File(path)))
  }

  def bracketReadFile(path: String): IO[Unit] =
    IO(s"opening file at $path").Debug *> openFileScanner(path)
      .onError(e => IO(e.getMessage).Debug.void)
      .bracket { scanner =>
        (IO.sleep(100.millis) *> IO.delay(scanner.nextLine())
          .Debug
          .onError(e => IO.delay(e.getMessage).Debug.void *> IO.delay(scanner.close())))
          .foreverM
      } { scanner => IO.delay(scanner.close()).void }

  val readFileFiber = for {
    fib <- bracketReadFile("/Users/aghurtchumelia/Personal/ce-ref-practice/src/main/scala/rtjvm/file.txt").start
    _ <- IO.sleep(1.second) *> IO("cancelling fiber after 1 second").Debug *> fib.cancel
  } yield ()

  /**
   * Resources
   */

  // example: read a file and then open connection, two resources that need to be opened
  // nesting resources are tedious
  def connFromConfig(path: String): IO[Unit] =
    openFileScanner(path).bracket { scanner =>
      // acquire a connection based on the file
      IO(new Connection(scanner.nextLine())).bracket { conn =>
        conn.open().Debug >> IO.never
      } { conn => conn.close().Debug.void }
    } { scanner => IO("closing file").Debug >> IO.delay(scanner.close()).void }

  // resource acquisition + resource release
  val connectionResource = Resource.make(IO(new Connection("rockthejvm.com"))) { _.close().Debug.void }
  // ... at a later part of your code

  val resourceFetchUrl = for {
    fib <- connectionResource.use(conn => conn.open() >> IO.never).start
    _ <- IO.sleep(1.second) >> fib.cancel
  } yield ()

  // resources are equivalent to brackets
  val simpleResource = IO("some resource")
  val usingResource: String => IO[String] = string => IO(s"using the string: $string").Debug
  val releaseResource: String => IO[Unit] = string => IO(s"finalizing the string: $string").Debug.void

  // equivalents here
  val usingResourceWithBracket: IO[String] = simpleResource.bracket(usingResource)(releaseResource)
  val usingResourceWithResource: IO[String] = Resource.make(simpleResource)(releaseResource).use(usingResource)

  /**
   * Exercise: read a text file with one line every 100 millis, using Resource
   * (refactor bracket exercise to use Resource)
   */

  def getResourceFromFile(path: String) =
    Resource.make(openFileScanner(path)) { scanner =>
      IO(s"closing file at $path").Debug >> IO(scanner.close())
    }

  def readLineByLine(): IO[Unit] = IO.unit

  def resourceReadFile(path: String) =
    IO(s"opening file at $path") >> getResourceFromFile(path).use { scanner =>
      readLineByLine()
    }

  // nested resources - flatMap
  def connFromConfResource(path: String) =
    Resource.make(openFileScanner(path)) { scanner =>
      IO("closing file").Debug >> IO(scanner.close())
    }.flatMap { scanner =>
      Resource.make(IO(new Connection(scanner.nextLine())))(conn => conn.close().void)
    }

  val openConnection = connFromConfResource("path").use(conn => conn.open() >> IO.never)
  // connection + file will close automatically

  val r = Resource.make(IO.unit)(_ => IO.never)

  // io with finalizer
  val ioWithFinalizer = IO(throw new RuntimeException("d")).Debug.guarantee(IO("I will always run").Debug.void)

  override val run: IO[Unit] = ioWithFinalizer.void
}
