package netty4s.core.server.api

import cats.effect.{Bracket, Sync}
import io.netty.channel.ChannelHandler
import netty4s.core.BracketT
import netty4s.core.server.netty.channel.SimpleWebsocketChannelHandler

trait HandlerCompiler[F[_]] {
  def compile(handler: WebsocketHandler[F]): ChannelHandler
}

object HandlerCompiler {
  def make[F[_]: Sync: BracketT](executor: Executor[F]): HandlerCompiler[F] = new DefaultHandlerCompiler[F](executor)
}

class DefaultHandlerCompiler[F[_]: Sync: BracketT](executor: Executor[F]) extends HandlerCompiler[F] {
  override def compile(handler: WebsocketHandler[F]): ChannelHandler = {
    handler match {
      case h: Handler.SimpleWebsocket[F] => {
        new SimpleWebsocketChannelHandler[F](h, executor)
      }
    }
  }
}
