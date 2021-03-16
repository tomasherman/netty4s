package netty4s.core.model

import io.netty.handler.codec.http.{FullHttpRequest, FullHttpResponse, HttpMethod}

object JTypes {
  type JHttpRequest = FullHttpRequest
  type JHttpResponse = FullHttpResponse
  type JHttpMethod = HttpMethod
}
