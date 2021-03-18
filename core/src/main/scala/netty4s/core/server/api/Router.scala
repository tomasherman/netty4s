package netty4s.core.server.api

import netty4s.core.model.HttpRequest

trait Router[F[_]] {
  def lookup(request: HttpRequest): ActionBuilder[F]
}

class PatMatRouter[F[_]](fn: PartialFunction[String, ActionBuilder[F]]) extends Router[F] {
  override def lookup(request: HttpRequest): ActionBuilder[F] = fn(request.uri)
}

object Router {
  def patmat[F[_]](fn: PartialFunction[String, ActionBuilder[F]]): Router[F] = new PatMatRouter[F](fn)
}