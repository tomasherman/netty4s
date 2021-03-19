package netty4s.examples.simple
import cats.effect.{ExitCode, IO}
import fs2.concurrent.Queue
import io.netty.handler.codec.http.websocketx.WebSocketFrame
import io.netty.util.ReferenceCountUtil
import netty4s.core.model.HttpRequest
import netty4s.core.server.api.Action.UpgradeWithWebsocket
import netty4s.core.server.api._
import netty4s.core.server.api.dsl.Dsl

import scala.util.Random

object SimpleExample extends cats.effect.IOApp {
  val dsl: Dsl[IO] = Dsl.of[IO]
  import dsl._

  override def run(args: List[String]): IO[ExitCode] = {
    val router = Router.patmat[IO] {
      case "/a/b/c" => respondWith(IO.delay(Ok()))
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
            case false => IO.pure(respond(Unauthorized()))
          }
        }
    }
    val app = HttpApp.fromRouter[IO] {
      router
    }
    ServerBuilder
      .localhost[IO]()
      .run(app)
      .map(_ => ExitCode.Success)
  }

  def auth(req: HttpRequest): IO[Boolean] = IO.delay(Random.nextBoolean())
}
