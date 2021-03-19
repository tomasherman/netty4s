package netty4s.core.server.api.dsl

import cats.effect.Sync

class Dsl[F[_]: Sync] extends RoutingDsl[F] with ResponseHttp11Dsl[F] with HandlersDsl[F] {
  val F: Sync[F] = Sync[F]
}

object Dsl {
  def of[F[_]: Sync] = new Dsl[F]
}
