package netty4s.core.server.api

import netty4s.core.server.api.ServerConfig.{Bind, Threading}

import java.net.InetAddress
import java.nio.file.Path

case class ServerConfig(bind: Bind, threading: Threading, keepAlive: Boolean)

object ServerConfig {
  sealed trait Bind
  case class TcpSocketAddress(host: InetAddress, port: Int) extends Bind
  case class UnixSocket(path: Path) extends Bind

  case class Threading(forceNio: Boolean, workerThreads: Option[Int])
  object Threading {
    def default: Threading =
      Threading(forceNio = false, workerThreads = Option.empty)
  }

  def localhost(port: Int): ServerConfig =
    ServerConfig(
      TcpSocketAddress(InetAddress.getLocalHost, port),
      Threading.default,
      keepAlive = true
    )
}
