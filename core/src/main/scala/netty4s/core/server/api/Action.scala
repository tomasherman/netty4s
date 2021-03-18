package netty4s.core.server.api

import netty4s.core.model.HttpResponse

sealed trait Action[+F[_]]

object Action {
  case class RespondWith(handler: HttpResponse) extends Action[Nothing]
  case class UpgradeWithWebsocket[F[_]](handler: WebsocketHandler[F]) extends Action[F]
}