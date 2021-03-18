package netty4s.examples.simple
import cats.effect.{ExitCode, IO}
import netty4s.core.model.HttpRequest
import netty4s.core.server.api.Action.{RespondWith, UpgradeWithWebsocket}
import netty4s.core.server.api.ServerBuilder.Config
import netty4s.core.server.api.{Dsl, Handler, HttpApp, Router, ServerBuilder}

object Main extends cats.effect.IOApp {

  val dsl = new Dsl[IO]
  import dsl._

  override def run(args: List[String]): IO[ExitCode] = {
    val router = Router.patmat[IO] {
      case "/a/b/c" => respondWith(IO.delay(???))
      case "/ws" => action { req =>
        auth(req).map {
          case true => UpgradeWithWebsocket(Handler.SimpleWebsocket[IO](???, ???))
          case false => RespondWith(???)
        }
      }
    }
    val app = HttpApp.fromRouter[IO] {
      router
    }
    ServerBuilder
      .fromConfig[IO](Config())
      .run(app).map(_ => ExitCode.Success)
  }

  def auth(req: HttpRequest): IO[Boolean] = IO.pure(true)
}
