package netty4s.core.model

import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.util.AsciiString

trait HttpResponse[+A] {
  def status: HttpResponseStatus
  def mapBody[B](f: A => B): HttpResponse[B]
  def headers: Map[String, String]
  def body: A
  def addHeaders(newHeaders: (String, String)*): HttpResponse[A]
}
case class Response(status: HttpResponseStatus, headers: Map[String, String]) extends HttpResponse[Nothing] {
  override def mapBody[B](f: Nothing => B): HttpResponse[B] = this
  override def body: Nothing = throw new UnsupportedOperationException
  def addHeaders(newHeaders: (String, String)*): HttpResponse[Nothing] = this.copy(headers = headers ++ newHeaders)
}
case class ResponseWithBody[A](status: HttpResponseStatus, body: A, headers: Map[String, String])
    extends HttpResponse[A] {
  def mapBody[B](f: A => B): HttpResponse[B] = copy(body = f(body))
  def addHeaders(newHeaders: (String, String)*): HttpResponse[A] = this.copy(headers = headers ++ newHeaders)
}

object HttpResponse {
  def apply(status: HttpResponseStatus): HttpResponse[Nothing] = Response(status, Map.empty)
  def apply[A](status: HttpResponseStatus, body: A): HttpResponse[A] = ResponseWithBody(status, body, Map.empty)
}
