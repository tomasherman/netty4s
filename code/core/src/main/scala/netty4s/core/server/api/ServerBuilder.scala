package netty4s.core.server.api

import cats.effect.Resource

trait ServerBuilder[F[_]] {
  def build(): Resource[F, Int]
  def run(app: HttpApp): F[Unit]
}

object ServerBuilder {
  case class Config()
  def fromConfig[F[_]](config: Config): ServerBuilder[F] = ???
}
