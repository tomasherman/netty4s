package netty4s.core.server.api

import netty4s.core.model.HttpRequest

class PatMatRouter[F[_]](fn: PartialFunction[String, ActionBuilder[F]]) extends Router[F] {
  override def lookup(request: HttpRequest): ActionBuilder[F] = fn(request.uri)
}
