package netty4s.examples.simple
import cats.effect.{ExitCode, IO}
import fs2.concurrent.Queue
import io.circe.Encoder
import io.netty.handler.codec.http.websocketx.WebSocketFrame
import io.netty.util.ReferenceCountUtil
import netty4s.core.model.HttpRequest
import netty4s.core.server.api.Action.{RespondWith, UpgradeWithWebsocket}
import netty4s.core.server.api._
import netty4s.core.server.api.dsl.Dsl
import wvlet.airframe._
import wvlet.log.{LogLevel, Logger}

import scala.util.Random

object SimpleExample extends cats.effect.IOApp {
  val dsl: Dsl[IO] = Dsl.of[IO]
  import dsl._

  case class ExampleJson(val1: String, val2: String)

  object ExampleJson {
    import io.circe.generic.semiauto.deriveEncoder
    implicit val encoder: Encoder[ExampleJson] = deriveEncoder[ExampleJson]
  }

  override def run(args: List[String]): IO[ExitCode] = {
    Logger.init
    Logger.setDefaultLogLevel(LogLevel.DEBUG)
    val router = Router.patmat[IO] {
      case "/a/b/c" => handleWith(Handler.serDe((_: String) => IO.delay(Ok("thx for calling"))))
      case "/test/serde" =>
        handleWith(Handler.serDe { (str: String) =>
          IO.delay {
            Ok(ExampleJson(str, str.toUpperCase))
          }
        })
      case "/ws" =>
        action { req =>
          auth(req).flatMap {
            case true =>
              Queue.bounded[IO, WebSocketFrame](100).map { q =>
                // RefCounting will hopefully not be necessary in real case, only used here due to echo-server nature of this handler
                val read = { (wsframe: WebSocketFrame) => q.enqueue1(ReferenceCountUtil.retain(wsframe)) }
                val write = q.dequeue1
                UpgradeWithWebsocket(readWriteWebsocket(read, write))
              }
            case false => IO.pure(RespondWith(Unauthorized()))
          }
        }
    }
    val app = HttpApp.fromRouter[IO] {
      router
    }
    ServerBuilder
      .fromConfig[IO](ServerConfig.localhost(8080).copy(keepAlive = false))
      .run(app)
      .map(_ => ExitCode.Success)
  }

  def auth(req: HttpRequest): IO[Boolean] = IO.delay(Random.nextBoolean())
}
