package netty4s.core.server.netty

import cats.effect.Sync
import io.netty.channel.{Channel, ChannelInitializer}
import io.netty.handler.codec.http.{HttpObjectAggregator, HttpServerCodec}
import netty4s.core.server.netty.channel.RoutingChannel

class Netty4sChannelInitializer[F[_]: Sync](
    routingChannelBuilder: () => RoutingChannel[F]
) extends ChannelInitializer[Channel] {
  override def initChannel(ch: Channel): Unit = {
    ch.pipeline()
      .addLast(new HttpServerCodec())
      .addLast(new HttpObjectAggregator(1024))
      .addLast(
        HandlerNames.ROUTER,
        routingChannelBuilder()
      )
  }
}

object Netty4sChannelInitializer {
  case class Config(maxContentLengthAggreggation: Int)
}
