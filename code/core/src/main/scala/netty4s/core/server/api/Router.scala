package netty4s.core.server.api

import netty4s.core.model.HttpRequest

trait Router[F[_]] {
  def lookup(request: HttpRequest): ActionBuilder[F]
}

object Router {
  def patmat[F[_]](fn: PartialFunction[String, ActionBuilder[F]]): Router[F] =
    new PatMatRouter[F](fn)
}
