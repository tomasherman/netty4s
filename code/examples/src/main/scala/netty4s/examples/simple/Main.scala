package netty4s.examples.simple
import cats.effect.{ExitCode, IO}
import netty4s.core.model.HttpRequest
import netty4s.core.server.api.Action.{RespondWith, UpgradeWithWebsocket}
import netty4s.core.server.api._
import fs2.concurrent.Queue
import io.netty.handler.codec.http.websocketx.WebSocketFrame
import io.netty.util.ReferenceCountUtil

object Main extends cats.effect.IOApp {

  val dsl = new Dsl[IO]
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
                UpgradeWithWebsocket(Handler.SimpleWebsocket[IO](read, write))
              }
            case false => IO.pure(respond(Ok()))
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

  def auth(req: HttpRequest): IO[Boolean] = IO.pure(true)
}
