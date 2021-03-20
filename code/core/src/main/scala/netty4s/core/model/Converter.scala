package netty4s.core.model

import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.handler.codec.http.{
  DefaultFullHttpResponse,
  DefaultHttpHeaders,
  EmptyHttpHeaders,
  HttpHeaders,
  HttpVersion
}
import netty4s.core.model.JTypes.JHttpResponse

object Converter {
  def response(httpResponse: HttpResponse[ByteBuf]): JHttpResponse = {
    httpResponse match {
      case Response(status, headers) =>
        new DefaultFullHttpResponse(
          HttpVersion.HTTP_1_1,
          status,
          Unpooled.buffer(0),
          convertHeaders(headers),
          EmptyHttpHeaders.INSTANCE
        )
      case ResponseWithBody(status, body, headers) =>
        new DefaultFullHttpResponse(
          HttpVersion.HTTP_1_1,
          status,
          body,
          convertHeaders(headers),
          EmptyHttpHeaders.INSTANCE
        )
    }
  }
  private def convertHeaders(headers: Map[String, String]): HttpHeaders = {
    val jheaders = new DefaultHttpHeaders()
    headers.foreach { headers =>
      jheaders.add(headers._1, headers._2)
    }
    jheaders
  }
}
