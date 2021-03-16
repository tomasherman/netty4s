package netty4s.core.server.api

import netty4s.core.server.api.requestmatch.RequestMatcher

case class Route[F[_]](matcher: RequestMatcher, handler: Handler[F])

object Route {
  def apply[F[_]](matcher: RequestMatcher): RouteBind[F] = new RouteBind[F] {
    override def to(handler: Handler[F]): Route[F] = ???
  }
}

trait RouteBind[F[_]] {
  def to(handler: Handler[F]): Route[F]
}
