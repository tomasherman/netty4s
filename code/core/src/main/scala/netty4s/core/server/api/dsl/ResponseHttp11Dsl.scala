package netty4s.core.server.api.dsl

import io.netty.handler.codec.http.{DefaultFullHttpResponse, HttpResponseStatus, HttpVersion}
import netty4s.core.model.HttpResponse

trait ResponseHttp11Dsl[F[_]] {
  private val version = HttpVersion.HTTP_1_1
  def Ok(): HttpResponse =
    HttpResponse(new DefaultFullHttpResponse(version, HttpResponseStatus.OK))
  def Unauthorized(): HttpResponse =
    HttpResponse(new DefaultFullHttpResponse(version, HttpResponseStatus.UNAUTHORIZED))
}
