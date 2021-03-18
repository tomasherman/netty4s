package netty4s.core.server.api

import io.netty.handler.codec.http.websocketx.WebSocketFrame

sealed trait Handler[F[_]]
sealed trait WebsocketHandler[F[_]] extends Handler[F]

object Handler {
  case class SimpleWebsocket[F[_]](incoming: WebSocketFrame => F[Unit], outgoing: F[WebSocketFrame]) extends WebsocketHandler[F]
}