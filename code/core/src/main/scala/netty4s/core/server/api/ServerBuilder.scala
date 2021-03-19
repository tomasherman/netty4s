package netty4s.core.server.api

import cats.effect.{ConcurrentEffect, Resource}
import netty4s.core.server.netty.NettyServerBuilder

trait ServerBuilder[F[_]] {
  def run(app: HttpApp[F]): F[Unit]
}

object ServerBuilder {

  def localhost[F[_]: ConcurrentEffect](port: Int = 8080): ServerBuilder[F] = {
    fromConfig(ServerConfig.localhost(port))
  }

  def fromConfig[F[_]: ConcurrentEffect](
      config: ServerConfig
  ): ServerBuilder[F] = new NettyServerBuilder[F](config)
}
