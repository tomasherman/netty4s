package netty4s.core.server.api.dsl

import io.netty.buffer.ByteBuf
import io.netty.handler.codec.http.{DefaultFullHttpResponse, HttpResponseStatus, HttpVersion}
import netty4s.core.model.HttpResponse
import netty4s.core.server.api.serde.Ser

trait ResponseHttp11Dsl[F[_]] {
  def Ok[O](o: O): HttpResponse[O] =
    HttpResponse[O](HttpResponseStatus.OK, o)
  def Unauthorized(): HttpResponse[ByteBuf] =
    HttpResponse(HttpResponseStatus.UNAUTHORIZED)
}
