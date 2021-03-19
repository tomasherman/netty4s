package netty4s.core.server.api

import cats.effect.Sync
import netty4s.core.model.{HttpRequest, HttpResponse}
import cats.syntax.functor._
import io.netty.handler.codec.http.{DefaultFullHttpResponse, HttpResponseStatus, HttpVersion}

class Dsl[F[_]: Sync] extends RoutingDsl[F] with ResponseHttp11Dsl[F] {
  val F: Sync[F] = Sync[F]
}

trait RoutingDsl[F[_]] {
  implicit val F: Sync[F]
  def respond(out: HttpResponse): Action[F] = Action.RespondWith[F](out)
  def respondWith(out: F[HttpResponse]): ActionBuilder[F] = response(_ => out)
  def response(f: HttpRequest => F[HttpResponse]): ActionBuilder[F] =
    ActionBuilder.lift(req => f(req).map(answer => Action.RespondWith(answer)))
  def upgradeToWebsocket(f: HttpRequest => F[HttpResponse]): ActionBuilder[F] =
    ActionBuilder.const(
      F.delay(Action.UpgradeWithWebsocket(Handler.SimpleWebsocket(???, ???)))
    )
  def action(f: HttpRequest => F[Action[F]]): ActionBuilder[F] =
    ActionBuilder.lift(f)
}

trait ResponseHttp11Dsl[F[_]] {
  private val version = HttpVersion.HTTP_1_1
  def Ok(): HttpResponse =
    HttpResponse(new DefaultFullHttpResponse(version, HttpResponseStatus.OK))
}
