package netty4s.core.model

import io.netty.buffer.ByteBuf
import netty4s.core.model.JTypes.{JHttpMethod, JHttpRequest}

case class HttpRequest(request: JHttpRequest) extends AnyVal {
  def method: JHttpMethod = request.method()
  def uri: String = request.uri()
  def content: ByteBuf = request.content()
}
