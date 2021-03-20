package netty4s.core.server.api.dsl

import cats.effect.Sync
import netty4s.core.model.HttpRequest
import netty4s.core.server.api.{Action, ActionBuilder, Handler}

trait RoutingDsl[F[_]] {
  implicit val F: Sync[F]
  def handleWith(handler: Handler[F]): ActionBuilder[F] = ActionBuilder.const(F.pure(Action.HandlerAction(handler)))
  def action(f: HttpRequest => F[Action[F]]): ActionBuilder[F] =
    ActionBuilder.lift(f)
}
