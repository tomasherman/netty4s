package netty4s.core.server.api.dsl

import cats.effect.Sync
import io.netty.handler.codec.http.websocketx.WebSocketFrame
import netty4s.core.server.api.Handler.ReadWriteWebSocket
import netty4s.core.server.api.WebsocketHandler

trait HandlersDsl[F[_]] {
  implicit val F: Sync[F]
  def readWriteWebsocket(read: WebSocketFrame => F[Unit], write: F[WebSocketFrame]): WebsocketHandler[F] =
    ReadWriteWebSocket(read, write, Sync[F].unit)
}
