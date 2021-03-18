package netty4s.core.server.api

import cats.effect.Sync
import netty4s.core.model.{HttpRequest, HttpResponse}
import cats.syntax.functor._
class Dsl[F[_]: Sync] {
  private val F: Sync[F] = Sync[F]
  def respondWith(out: F[HttpResponse]): ActionBuilder[F] = response(_ => out)
  def response(f: HttpRequest => F[HttpResponse]): ActionBuilder[F] = ActionBuilder.lift(req => f(req).map(answer => Action.RespondWith(answer)))
  def upgradeToWebsocket(f: HttpRequest => F[HttpResponse]): ActionBuilder[F] = ActionBuilder.const(F.delay(Action.UpgradeWithWebsocket(Handler.SimpleWebsocket(???, ???))))
  def action(f: HttpRequest => F[Action[F]]): ActionBuilder[F] = ActionBuilder.lift(f)
}
