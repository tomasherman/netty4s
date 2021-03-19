package netty4s.core.server.netty

import cats.effect.{Concurrent, Resource, Sync}
import io.netty.channel.epoll.{EpollEventLoopGroup, EpollServerDomainSocketChannel, EpollServerSocketChannel}
import io.netty.channel.kqueue.{KQueueEventLoopGroup, KQueueServerDomainSocketChannel}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.unix.DomainSocketAddress
import io.netty.channel.{EventLoopGroup, ServerChannel}
import netty4s.core.server.netty.TransportSpecifics.Specifics
import netty4s.core.server.netty.utils.FutureListeners

import java.net.{InetAddress, InetSocketAddress, SocketAddress}
import java.nio.file.Path

class TransportSpecifics[F[_]: Concurrent] {
  private val F: Sync[F] = Sync[F]
  def epollUnixSocket(socketPath: Path, workerThreadCount: Int): Specifics[F, EpollServerDomainSocketChannel] = {
    Specifics(
      classOf[EpollServerDomainSocketChannel],
      new DomainSocketAddress(socketPath.toFile),
      eventLoopGroup(new EpollEventLoopGroup(1)),
      eventLoopGroup(new EpollEventLoopGroup(workerThreadCount))
    )
  }

  def kqueueUnixSocket(socketPath: Path, workerThreadCount: Int): Specifics[F, KQueueServerDomainSocketChannel] = {
    Specifics(
      classOf[KQueueServerDomainSocketChannel],
      new DomainSocketAddress(socketPath.toFile),
      eventLoopGroup(new KQueueEventLoopGroup(1)),
      eventLoopGroup(new KQueueEventLoopGroup(workerThreadCount))
    )
  }

  def epoll(address: InetAddress, port: Int, workerThreadCount: Int): Specifics[F, EpollServerSocketChannel] = {
    Specifics(
      classOf[EpollServerSocketChannel],
      new InetSocketAddress(address, port),
      eventLoopGroup(new EpollEventLoopGroup(1)),
      eventLoopGroup(new EpollEventLoopGroup(workerThreadCount))
    )
  }
  def nio(address: InetAddress, port: Int, workerThreadCount: Int): Specifics[F, NioServerSocketChannel] = {
    Specifics(
      classOf[NioServerSocketChannel],
      new InetSocketAddress(address, port),
      eventLoopGroup(new NioEventLoopGroup(1)),
      eventLoopGroup(new NioEventLoopGroup(workerThreadCount))
    )
  }

  private def eventLoopGroup[E <: EventLoopGroup](
      group: => EventLoopGroup
  ): Resource[F, EventLoopGroup] = {
    Resource.make(F.delay(group))(g => F.delay(FutureListeners.toF(g.shutdownGracefully())))
  }

}

object TransportSpecifics {
  case class Specifics[F[_], C <: ServerChannel](
      serverChannelClass: Class[C],
      socketAddress: SocketAddress,
      serverGroup: Resource[F, EventLoopGroup],
      workerGroup: Resource[F, EventLoopGroup]
  )

  def default[F[_]: Concurrent] = new TransportSpecifics[F]

}
