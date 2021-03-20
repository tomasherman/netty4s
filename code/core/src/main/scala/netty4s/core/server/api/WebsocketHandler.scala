package netty4s.core.server.api

import cats.effect.Sync
import io.netty.handler.codec.http.websocketx.WebSocketFrame
import netty4s.core.model.{HttpRequest, HttpResponse}
import netty4s.core.server.api.serde.{BodyEncoder, DeSer, Ser}
import cats.syntax.functor._
import io.netty.buffer.{ByteBuf, ByteBufAllocator}
import io.netty.util.{ReferenceCountUtil, ReferenceCounted}

sealed trait Handler[F[_]] {
  def eval(req: HttpRequest, allocator: ByteBufAllocator): F[HttpResponse[ByteBuf]]
}

object Handler {
  def lift[F[_]](f: HttpRequest => F[HttpResponse[ByteBuf]]): Handler[F] =
    new Handler[F] {
      override def eval(req: HttpRequest, allocator: ByteBufAllocator): F[HttpResponse[ByteBuf]] = f(req)
    }

  def serDe[F[_]: Sync, I: DeSer, O: Ser](f: I => F[HttpResponse[O]]): Handler[F] =
    new Handler[F] {
      override def eval(req: HttpRequest, allocator: ByteBufAllocator): F[HttpResponse[ByteBuf]] = {
        val i = DeSer[I].deserialize(req.content)
        ReferenceCountUtil.release(req.request)
        f(i).map(resp => BodyEncoder.encode[O](resp, allocator))
      }
    }
}

sealed trait WebsocketHandler[F[_]]

object WebsocketHandler {
  case class ReadWriteWebSocket[F[_]](
      incoming: WebSocketFrame => F[Unit],
      outgoing: F[WebSocketFrame],
      onClose: F[Unit]
  ) extends WebsocketHandler[F]
}
