package netty4s.core.server.api

import io.netty.channel.ChannelHandler

trait HandlerCompiler[F[_]] {
  def compile(handler: Handler[F]): ChannelHandler
}

class DefaultHandlerCompiler[F[_]] extends HandlerCompiler[F] {
  override def compile(handler: Handler[F]): ChannelHandler = {
    handler match {
      case Handler.SimpleWebsocket(in, out) => ???
    }
  }
}