package netty4s.core.server.netty

import cats.effect.{Concurrent, ConcurrentEffect, Resource, Sync}
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.epoll.Epoll
import io.netty.channel.kqueue.KQueue
import io.netty.channel.{EventLoopGroup, ServerChannel}
import netty4s.core.server.api.ServerConfig.{TcpSocketAddress, UnixSocket}
import netty4s.core.server.api._
import netty4s.core.server.netty.channel.RoutingChannel
import netty4s.core.server.netty.utils.FutureListeners

class NettyServerBuilder[F[_]: ConcurrentEffect](config: ServerConfig) extends ServerBuilder[F] {
  private val F: Sync[F] = Sync[F]

  private def build(httpApp: HttpApp[F]): Resource[F, Unit] = {
    val workerThreadCount = config.threading.workerThreads.getOrElse(0)
    val specifics = config.bind match {
      case UnixSocket(path) =>
        if (Epoll.isAvailable) {
          TransportSpecifics.default[F].epollUnixSocket(path, workerThreadCount)
        } else if (KQueue.isAvailable) {
          TransportSpecifics.default[F].kqueueUnixSocket(path, workerThreadCount)
        } else {
          throw new Exception("Unix socket is supported only on systems with Epoll or KQeueue")
        }
      case TcpSocketAddress(address, port) =>
        if (Epoll.isAvailable) {
          TransportSpecifics.default[F].epoll(address, port, workerThreadCount)
        } else {
          TransportSpecifics.default[F].nio(address, port, workerThreadCount)
        }
    }

    for {
      srvGrp <- specifics.serverGroup
      wrkGrp <- specifics.workerGroup
      bootstrap = makeBootstrap(srvGrp, wrkGrp, specifics.serverChannelClass, httpApp, Executor.catsEffect[F])
      _ <- Resource.liftF(
        FutureListeners.cancellable(
          bootstrap.bind(specifics.socketAddress).channel().closeFuture()
        )
      )
    } yield {
      ()
    }

  }

  private def makeBootstrap[C <: ServerChannel](
      parentGroup: EventLoopGroup,
      workerGroup: EventLoopGroup,
      serverChannel: Class[C],
      httpApp: HttpApp[F],
      executor: Executor[F]
  ): ServerBootstrap = {
    val routingChannel = new RoutingChannel[F](
      httpApp.asRouter,
      HandlerCompiler.make(executor),
      executor,
      RoutingChannel.Config.apply(keepAlive = config.keepAlive)
    )

    new ServerBootstrap()
      .group(parentGroup, workerGroup)
      .channel(serverChannel)
      .childHandler(
        new Netty4sChannelInitializer[F](
          routingChannel
        )
      )
  }

  override def run(app: HttpApp[F]): F[Unit] = {
    build(app).use(_ => Concurrent[F].never)
  }
}

object NettyServerBuilder {}
