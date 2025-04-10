package sttp

import cats.effect.{IO, IOApp}
import cats.implicits.toBifunctorOps
import sttp.client3._
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend

import scala.concurrent.duration.DurationInt

object SimpleSttpClient extends IOApp.Simple {

  /***
   * basicRequest.auth
     .basic(systemCredentials.username, systemCredentials.password)
     .response(asString)
     .post(uri)
     .contentType(MediaType.ApplicationJson)
     .body(PlayJsonJsoniter.serializeToStr(WritesCasinoTableFilterPayload.writes(payload)))
     .send(backend)
     .map {
          _.body
            // TODO: default or raise error?
            .leftMap(error => s"failed to send request with body: $payload. error: $error")
            .flatMap(fromJson[TablesLoaded])
        }
        .flatMap {
          case Right(tablesLoaded) => tablesLoaded.pure[F]
          // TODO: default or raise error?
          case Left(error) => log.error(error).as(TablesLoaded(Map.empty))
        }
        .handleErrorWith { error =>
          log.error(s"http call failed during loading tables: $error").as(TablesLoaded(Map.empty))
        }
   */

  def run: IO[Unit] = {
    // Define the request
    val base = "http://127.0.0.1:9000"
    val postfix = "/org/quantori"
    val uri = uri"${base.concat(postfix)}"

    val request = basicRequest
      .get(uri)
      .response(asString)


    def httpCall[T](backend: SttpBackend[IO, Any]): IO[Response[Either[String, String]]] = {

      IO.sleep(1.second) >> IO.raiseError(throw new RuntimeException("boom"))
      request.send(backend)
    }


    // Use the backend and send the request
    AsyncHttpClientCatsBackend.resource[IO]().use { backend =>
      for {
        a <- httpCall(backend)
          .map {
            _.body
              .leftMap(error => s"failed to send request with body: error: $error")
              .flatMap {s =>
                println("success")

                Right(s.toUpperCase)
              }
          }.flatMap {
            case Right(got) =>
              IO.println("parsed and accumulated") *>
                IO.pure(got)
            case Left(e) => IO.pure("returning default since serialization failed")
          }.handleErrorWith { error =>
            IO.println(error.getMessage) *>
              IO.println(s"http call failed during loading tables: $error").as("another default since exception")
          }
        _ <- IO.println(s"final data: $a")
      } yield ()
    }
  }
}

