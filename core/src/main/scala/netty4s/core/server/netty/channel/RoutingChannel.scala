package netty4s.core.server.netty.channel

import cats.effect.Sync
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.websocketx.{WebSocketFrameAggregator, WebSocketServerProtocolHandler}
import netty4s.core.model.JTypes.JHttpRequest
import netty4s.core.model.{HttpRequest, HttpResponse}
import netty4s.core.server.api.{Action, HandlerCompiler, Router, WebsocketHandler}
import cats.syntax.flatMap._

class RoutingChannel[F[_]: Sync](router: Router[F], handlerCompiler: HandlerCompiler[F]) extends SimpleChannelInboundHandler[FullHttpRequest](false){
  private val F: Sync[F] = Sync[F]

  override def channelRead0(ctx: ChannelHandlerContext, jrequest: JHttpRequest): Unit = {
    val request = HttpRequest(jrequest)
    val action: F[Action[F]] = router.lookup(request).build(request)
    action.flatMap {
      case Action.RespondWith(response) => respondWith(ctx, response)
      case Action.UpgradeWithWebsocket(handler) => upgradeToWebsocket(ctx, request.uri, handler.asInstanceOf[WebsocketHandler[F]]) // scala3 bug?
    }
  }

  private def upgradeToWebsocket(ctx: ChannelHandlerContext, uri: String, handler: WebsocketHandler[F]): F[Unit] = {
    F.delay {
      ctx.executor().execute { () =>
        ctx.pipeline().addLast(new WebSocketServerProtocolHandler(uri))
        ctx.pipeline().addLast(new WebSocketFrameAggregator(1024))
        ctx.pipeline().addLast(handlerCompiler.compile(handler))
      }
    }
  }

  private def respondWith(ctx: ChannelHandlerContext, r: HttpResponse): F[Unit] = {
    F.delay(ctx.writeAndFlush(r.response))
  }

}
