package netty4s.core.server.netty

import cats.effect.{Concurrent, ConcurrentEffect, Resource, Sync}
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.epoll.{Epoll, EpollEventLoopGroup, EpollServerDomainSocketChannel, EpollServerSocketChannel}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.unix.DomainSocketAddress
import io.netty.channel.{EventLoopGroup, ServerChannel}
import netty4s.core.server.api.ServerConfig.{TcpSocketAddress, UnixSocket}
import netty4s.core.server.api.{Executor, HandlerCompiler, HttpApp, ServerBuilder, ServerConfig}
import netty4s.core.server.netty.utils.FutureListeners

import java.net.InetSocketAddress

class NettyServerBuilder[F[_]: ConcurrentEffect](config: ServerConfig) extends ServerBuilder[F] {
  private val F: Sync[F] = Sync[F]
  private def build(httpApp: HttpApp[F]): Resource[F, Unit] = {
    val threadCount = config.threading.workerThreads.getOrElse(0)
    val (serverClass, socketAddress, serverGroup, workerGroup) =
      config.bind match {
        case UnixSocket(path) =>
          if (Epoll.isAvailable) {
            (
              classOf[EpollServerDomainSocketChannel],
              new DomainSocketAddress(path.toFile),
              eventLoopGroup(new EpollEventLoopGroup(1)),
              eventLoopGroup(new EpollEventLoopGroup(threadCount))
            )
          } else ???
        case TcpSocketAddress(address, port) =>
          if (Epoll.isAvailable) {
            (
              classOf[EpollServerSocketChannel],
              new InetSocketAddress(address, port),
              eventLoopGroup(new EpollEventLoopGroup(1)),
              eventLoopGroup(new EpollEventLoopGroup(threadCount))
            )
          } else {
            (
              classOf[NioServerSocketChannel],
              new InetSocketAddress(address, port),
              eventLoopGroup(new NioEventLoopGroup(1)),
              eventLoopGroup(new NioEventLoopGroup(threadCount))
            )
          }
      }
    for {
      srvGrp <- serverGroup
      wrkGrp <- workerGroup
      bootstrap = makeBootstrap(srvGrp, wrkGrp, serverClass, httpApp, Executor.catsEffect[F])
      _ <- Resource.liftF(
        FutureListeners.toF(
          bootstrap.bind(socketAddress).channel().closeFuture()
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
    new ServerBootstrap()
      .group(parentGroup, workerGroup)
      .channel(serverChannel)
      .childHandler(
        new Netty4sChannelInitializer[F](
          httpApp,
          HandlerCompiler.make(executor),
          executor
        )
      )
  }

  private def eventLoopGroup[E <: EventLoopGroup](
      group: => EventLoopGroup
  ): Resource[F, EventLoopGroup] = {
    Resource.make(F.delay(group))(g => F.delay(FutureListeners.toF(g.shutdownGracefully())))
  }

  override def run(app: HttpApp[F]): F[Unit] = {
    build(app).use(_ => Concurrent[F].never)
  }
}
