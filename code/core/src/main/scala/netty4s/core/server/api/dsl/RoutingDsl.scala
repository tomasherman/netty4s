package netty4s.core.server.api.dsl

import cats.effect.Sync
import netty4s.core.model.{HttpRequest, HttpResponse}
import netty4s.core.server.api.{Action, ActionBuilder, Handler}
import cats.syntax.functor._
trait RoutingDsl[F[_]] {
  implicit val F: Sync[F]
  def respond(out: HttpResponse): Action[F] = Action.RespondWith[F](out)
  def respondWith(out: F[HttpResponse]): ActionBuilder[F] = response(_ => out)
  def response(f: HttpRequest => F[HttpResponse]): ActionBuilder[F] =
    ActionBuilder.lift(req => f(req).map(answer => Action.RespondWith(answer)))
  def action(f: HttpRequest => F[Action[F]]): ActionBuilder[F] =
    ActionBuilder.lift(f)
}
