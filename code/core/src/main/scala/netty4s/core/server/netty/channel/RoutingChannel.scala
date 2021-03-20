package netty4s.core.server.netty.channel

import cats.effect.Sync
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.websocketx.{WebSocketFrameAggregator, WebSocketServerProtocolHandler}
import netty4s.core.model.JTypes.JHttpRequest
import netty4s.core.model.{HttpRequest, HttpResponse, JTypes}
import netty4s.core.server.api.{Action, Executor, Handler, HandlerCompiler, Router, WebsocketHandler}
import cats.syntax.flatMap._
import io.netty.buffer.ByteBuf

class RoutingChannel[F[_]: Sync](
    router: Router[F],
    handlerCompiler: HandlerCompiler[F],
    executor: Executor[F],
    config: RoutingChannel.Config
) extends SimpleChannelInboundHandler[FullHttpRequest](false) {
  private val F: Sync[F] = Sync[F]

  override def channelRead0(
      ctx: ChannelHandlerContext,
      jrequest: JHttpRequest
  ): Unit = {
    val request = HttpRequest(jrequest)
    val action: F[Action[F]] = router.lookup(request).build(request)
    val f = action.flatMap {
      case Action.HandlerAction(handler) => evalHandler(ctx, request, handler)
      case Action.RespondWith(response)  => respondWith(ctx, response)
      case Action.UpgradeWithWebsocket(handler) =>
        upgradeToWebsocket(ctx, request, request.uri, handler)
    }
    executor.fireAndForget(f)
  }

  private def evalHandler(ctx: ChannelHandlerContext, req: HttpRequest, handler: Handler[F]): F[Unit] = {
    handler.eval(req, ctx.alloc()).flatMap(respondWith(ctx, _))
  }

  private def upgradeToWebsocket(
      ctx: ChannelHandlerContext,
      msg: HttpRequest,
      uri: String,
      handler: WebsocketHandler[F]
  ): F[Unit] = {
    F.delay {
      ctx.executor().execute { () =>
        ctx.pipeline().addLast(new WebSocketServerProtocolHandler(uri))
        ctx.pipeline().addLast(new WebSocketFrameAggregator(1024))
        ctx.pipeline().addLast(handlerCompiler.compile(handler))
        ctx.fireChannelRead(msg.request)
      }
    }
  }

  private def respondWith(ctx: ChannelHandlerContext, r: HttpResponse[ByteBuf]): F[Unit] = {
    F.delay {
      ctx.executor().execute { () =>
        ctx.writeAndFlush(JTypes.scalaToJava(r))
        if (!config.keepAlive) {
          ctx.close()
        }
      }
    }
  }
}

object RoutingChannel {
  case class Config(keepAlive: Boolean)
  object Config {
    val default: Config = Config(keepAlive = false)
  }
}
