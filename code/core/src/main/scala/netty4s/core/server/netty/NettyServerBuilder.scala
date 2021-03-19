package netty4s.core.server.netty

import cats.effect.{Concurrent, ConcurrentEffect, Resource, Sync}
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.epoll.{Epoll, EpollEventLoopGroup, EpollServerDomainSocketChannel, EpollServerSocketChannel}
import io.netty.channel.kqueue.{KQueue, KQueueEventLoopGroup, KQueueServerDomainSocketChannel}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.ServerSocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.unix.DomainSocketAddress
import io.netty.channel.{EventLoopGroup, ServerChannel}
import netty4s.core.server.api.ServerConfig.{TcpSocketAddress, UnixSocket}
import netty4s.core.server.api.{Executor, HandlerCompiler, HttpApp, ServerBuilder, ServerConfig}
import netty4s.core.server.netty.NettyServerBuilder.TransportSpecific
import netty4s.core.server.netty.utils.FutureListeners

import java.net.{InetAddress, InetSocketAddress, SocketAddress}
import java.nio.file.Path

class NettyServerBuilder[F[_]: ConcurrentEffect](config: ServerConfig) extends ServerBuilder[F] {
  private val F: Sync[F] = Sync[F]
  private def build(httpApp: HttpApp[F]): Resource[F, Unit] = {
    val workerThreadCount = config.threading.workerThreads.getOrElse(0)
    val specifics = config.bind match {
      case UnixSocket(path) =>
        if (Epoll.isAvailable) {
          epollUnixSocket(path, workerThreadCount)
        } else if (KQueue.isAvailable) {
          kqueueUnixSocket(path, workerThreadCount)
        } else {
          throw new Exception("Unix socket is supported only on systems with Epoll or KQeueue")
        }
      case TcpSocketAddress(address, port) =>
        if (Epoll.isAvailable) {
          epoll(address, port, workerThreadCount)
        } else {
          nio(address, port, workerThreadCount)
        }
    }

    for {
      srvGrp <- specifics.serverGroup
      wrkGrp <- specifics.workerGroup
      bootstrap = makeBootstrap(srvGrp, wrkGrp, specifics.serverChannelClass, httpApp, Executor.catsEffect[F])
      _ <- Resource.liftF(
        FutureListeners.toF(
          bootstrap.bind(specifics.socketAddress).channel().closeFuture()
        )
      )
    } yield {
      ()
    }

  }

  private def epollUnixSocket(socketPath: Path, workerThreadCount: Int) = {
    TransportSpecific(
      classOf[EpollServerDomainSocketChannel],
      new DomainSocketAddress(socketPath.toFile),
      eventLoopGroup(new EpollEventLoopGroup(1)),
      eventLoopGroup(new EpollEventLoopGroup(workerThreadCount))
    )
  }

  private def kqueueUnixSocket(socketPath: Path, workerThreadCount: Int) = {
    TransportSpecific(
      classOf[KQueueServerDomainSocketChannel],
      new DomainSocketAddress(socketPath.toFile),
      eventLoopGroup(new KQueueEventLoopGroup(1)),
      eventLoopGroup(new KQueueEventLoopGroup(workerThreadCount))
    )
  }

  private def epoll(address: InetAddress, port: Int, workerThreadCount: Int) = {
    TransportSpecific(
      classOf[EpollServerSocketChannel],
      new InetSocketAddress(address, port),
      eventLoopGroup(new EpollEventLoopGroup(1)),
      eventLoopGroup(new EpollEventLoopGroup(workerThreadCount))
    )
  }
  private def nio(address: InetAddress, port: Int, workerThreadCount: Int) = {
    TransportSpecific(
      classOf[NioServerSocketChannel],
      new InetSocketAddress(address, port),
      eventLoopGroup(new NioEventLoopGroup(1)),
      eventLoopGroup(new NioEventLoopGroup(workerThreadCount))
    )
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

object NettyServerBuilder {
  case class TransportSpecific[F[_], C <: ServerChannel](
      serverChannelClass: Class[C],
      socketAddress: SocketAddress,
      serverGroup: Resource[F, EventLoopGroup],
      workerGroup: Resource[F, EventLoopGroup]
  )
}
