package netty4s.core.model

import io.netty.buffer.ByteBuf
import io.netty.handler.codec.http.websocketx.WebSocketFrame
import io.netty.handler.codec.http.{FullHttpRequest, FullHttpResponse, HttpMethod}

object JTypes {
  type JHttpRequest = FullHttpRequest
  type JHttpResponse = FullHttpResponse
  type JHttpMethod = HttpMethod
  type JWebsocketFrame = WebSocketFrame

  def scalaToJava(scalaResponse: HttpResponse[ByteBuf]): JHttpResponse = {
    Converter.response(scalaResponse)
  }

}
