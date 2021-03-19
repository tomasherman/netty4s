package netty4s.core.server.api

import io.netty.handler.codec.http.websocketx.WebSocketFrame

sealed trait Handler
sealed trait WebsocketHandler[F[_]] extends Handler

object Handler {
  case class ReadWriteWebSocket[F[_]](
      incoming: WebSocketFrame => F[Unit],
      outgoing: F[WebSocketFrame],
      onClose: F[Unit]
  ) extends WebsocketHandler[F]
}
