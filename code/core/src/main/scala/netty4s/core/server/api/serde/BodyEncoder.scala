package netty4s.core.server.api.serde

import io.netty.buffer.{ByteBuf, ByteBufAllocator}
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.util.AsciiString
import netty4s.core.model.HttpResponse

object BodyEncoder {
  def encode[A: Ser](response: HttpResponse[A], allocator: ByteBufAllocator): HttpResponse[ByteBuf] = {
    val bodyBytes = Ser[A].serialize(response.body, allocator)
    val contentLength = bodyBytes.readableBytes()
    val contentType = Ser[A].contentType
    response
      .addHeaders(
        HttpHeaderNames.CONTENT_LENGTH.toString -> String.valueOf(contentLength),
        HttpHeaderNames.CONTENT_TYPE.toString -> contentType.toString
      )
      .mapBody(_ => bodyBytes)
  }
}
