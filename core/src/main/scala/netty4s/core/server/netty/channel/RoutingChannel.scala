package netty4s.core.server.netty.channel

import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.FullHttpRequest
import netty4s.core.model.HttpRequest
import netty4s.core.server.api.Router

class RoutingChannel[F[_]](router: Router[F]) extends SimpleChannelInboundHandler[FullHttpRequest](false){
  override def channelRead0(ctx: ChannelHandlerContext, msg: FullHttpRequest): Unit = {
    val foundHandler = router.lookup(HttpRequest(msg))
    //ctx.pipeline().addLast(???)
    ctx.fireChannelRead(msg)
  }
}
