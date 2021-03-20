package netty4s.core.server.api

import io.netty.buffer.ByteBuf
import netty4s.core.model.HttpResponse

sealed trait Action[F[_]]

object Action {
  case class HandlerAction[F[_]](handler: Handler[F]) extends Action[F]
  case class RespondWith[F[_]](handler: HttpResponse[ByteBuf]) extends Action[F]
  case class UpgradeWithWebsocket[F[_]](handler: WebsocketHandler[F]) extends Action[F]
}
