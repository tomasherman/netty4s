package netty4s.core.server.api

import netty4s.core.model.HttpRequest

trait ActionBuilder[F[_]] {
  def build(httpRequest: HttpRequest): F[Action[F]]
}

object ActionBuilder {
  def const[F[_]](action: F[Action[F]]): ActionBuilder[F] = _ => action
  def lift[F[_]](f: HttpRequest => F[Action[F]]): ActionBuilder[F] =
    (httpRequest: HttpRequest) => f(httpRequest)
}
