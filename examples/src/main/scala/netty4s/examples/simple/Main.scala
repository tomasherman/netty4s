package netty4s.examples.simple
import cats.effect.{ExitCode, IO}
import netty4s.core.server.api.ServerBuilder.Config
import netty4s.core.server.api.requestmatch.MethodMatcher._
import netty4s.core.server.api.{Handler, HttpApp, Route, Router, ServerBuilder}

object Main extends cats.effect.IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val router = Router.simple[IO](
      Route(Get -> "v1/test").to(Handler.http[IO](request => IO.pure(???))),
      Route(Get -> "v1/test").to(Handler.http[IO](request => IO.pure(???)))
    )
    val app = HttpApp.fromRouter[IO] {
      router
    }
    ServerBuilder
      .fromConfig[IO](Config())
      .run(app).map(_ => ExitCode.Success)
  }
}
