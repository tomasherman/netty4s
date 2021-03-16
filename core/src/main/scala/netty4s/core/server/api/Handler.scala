package netty4s.core.server.api

import netty4s.core.model.{HttpRequest, HttpResponse}

sealed trait Handler[F[_]]

object Handler {
  case class SimpleResponse[F[_]](compute: HttpRequest => F[HttpResponse]) extends Handler[F]

  def http[F[_]](f: HttpRequest => F[HttpResponse]) = new SimpleResponse[F](f)
}