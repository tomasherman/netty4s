package netty4s.core.server.netty.channel

import cats.Monad
import cats.effect.{Bracket, Concurrent, Sync}
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.websocketx.WebSocketFrame
import netty4s.core.server.api.WebsocketHandler.ReadWriteWebSocket
import netty4s.core.server.api.Executor
import cats.effect.syntax.bracket._
import io.netty.util.ReferenceCountUtil
import cats.syntax.flatMap._
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler.{
  HandshakeComplete,
  ServerHandshakeStateEvent
}
import netty4s.core.server.netty.HandlerNames
import netty4s.core.server.netty.utils.FutureListeners

import scala.annotation.nowarn

class ReadWriteWebsocketChannelHandler[F[_]: Concurrent](
    handler: ReadWriteWebSocket[F],
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
        onSuccessfulHandshake(ctx)
      }
      case other =>
        throw new Exception(s"Received unexpected UserEvent: $other for channel: ${ctx.channel()}")
    }
  }

  override def channelRead0(ctx: ChannelHandlerContext, msg: WebSocketFrame): Unit = {
    handleRead(msg)
  }

  private def onSuccessfulHandshake(ctx: ChannelHandlerContext): Unit = {
    ctx.pipeline().remove(HandlerNames.ROUTER)
    val awaitChannelClose = registerChannelClose(ctx)
    val writeLoopTask = writeLoop(ctx)
    val cancelledWriteLoop = Concurrent[F].race(awaitChannelClose, writeLoopTask) >> handler.onClose
    executor.fireAndForget(cancelledWriteLoop)
  }

  private def registerChannelClose(ctx: ChannelHandlerContext): F[Void] = {
    FutureListeners.uncancellable[F, Void](
      ctx.channel().closeFuture().addListener(FutureListeners.channelClosedLog(ctx.channel()))
    )
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
