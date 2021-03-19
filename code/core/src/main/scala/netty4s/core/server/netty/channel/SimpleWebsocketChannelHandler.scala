package netty4s.core.server.netty.channel

import cats.Monad
import cats.effect.{Bracket, Sync}
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.websocketx.WebSocketFrame
import netty4s.core.server.api.Handler.SimpleWebsocket
import netty4s.core.server.api.Executor
import cats.effect.syntax.bracket._
import io.netty.util.ReferenceCountUtil
import cats.syntax.flatMap._
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler.{
  HandshakeComplete,
  ServerHandshakeStateEvent
}
import netty4s.core.server.netty.HandlerNames

import scala.annotation.nowarn

class SimpleWebsocketChannelHandler[F[_]: Sync](
    handler: SimpleWebsocket[F],
    executor: Executor[F]
)(implicit b: Bracket[F, Throwable])
    extends SimpleChannelInboundHandler[WebSocketFrame](false) {
  private val F: Sync[F] = Sync[F]

  @nowarn
  override def userEventTriggered(ctx: ChannelHandlerContext, evt: Any): Unit = {
    evt match {
      case ServerHandshakeStateEvent.HANDSHAKE_COMPLETE =>
      // ignore deprecated
      case _: HandshakeComplete => {
        ctx.pipeline().remove(HandlerNames.ROUTER)
        executor.fireAndForget(writeLoop(ctx))
      }
    }
  }

  override def channelRead0(ctx: ChannelHandlerContext, msg: WebSocketFrame): Unit = {
    handleRead(msg)
  }

  private def writeLoop(ctx: ChannelHandlerContext): F[Unit] = {
    val action = handler.outgoing.flatMap(writeToChannel(ctx, _))
    Monad[F].foreverM[Unit, Unit](action)
  }

  def writeToChannel(ctx: ChannelHandlerContext, msg: WebSocketFrame): F[Unit] =
    F.delay {
      ctx.writeAndFlush(msg)
    }

  private def handleRead(msg: WebSocketFrame): Unit = {
    executor.fireAndForget(
      handler.incoming(msg).guarantee(F.delay(ReferenceCountUtil.release(msg)))
    )
  }
}
