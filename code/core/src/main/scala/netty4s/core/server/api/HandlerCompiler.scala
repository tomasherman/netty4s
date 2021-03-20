package netty4s.core.server.api

import cats.effect.{Bracket, Concurrent, Sync}
import io.netty.channel.ChannelHandler
import netty4s.core.BracketT
import netty4s.core.server.netty.channel.ReadWriteWebsocketChannelHandler

trait HandlerCompiler[F[_]] {
  def compile(handler: WebsocketHandler[F]): ChannelHandler
}

object HandlerCompiler {
  def make[F[_]: Concurrent: BracketT](executor: Executor[F]): HandlerCompiler[F] =
    new DefaultHandlerCompiler[F](executor)
}

class DefaultHandlerCompiler[F[_]: Concurrent: BracketT](executor: Executor[F]) extends HandlerCompiler[F] {
  override def compile(handler: WebsocketHandler[F]): ChannelHandler = {
    handler match {
      case h: WebsocketHandler.ReadWriteWebSocket[F] => {
        new ReadWriteWebsocketChannelHandler[F](h, executor)
      }
    }
  }
}
