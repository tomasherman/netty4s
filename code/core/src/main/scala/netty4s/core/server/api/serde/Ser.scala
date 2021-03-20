package netty4s.core.server.api.serde

import io.netty.buffer.{ByteBuf, ByteBufAllocator, ByteBufUtil}
import io.netty.handler.codec.http.HttpHeaderValues
import io.netty.util.AsciiString

trait Ser[A] { self =>
  def contentType: AsciiString
  def serialize(a: A, allocator: ByteBufAllocator): ByteBuf
  def contraMap[B](f: B => A): Ser[B] =
    new Ser[B] {
      override def contentType: AsciiString = self.contentType

      override def serialize(a: B, allocator: ByteBufAllocator): ByteBuf = self.serialize(f(a), allocator)
    }
  def contraMap[B](f: B => A, newContentType: AsciiString): Ser[B] =
    new Ser[B] {
      override def contentType: AsciiString = newContentType

      override def serialize(a: B, allocator: ByteBufAllocator): ByteBuf = self.serialize(f(a), allocator)
    }
}

object Ser {
  def apply[A: Ser]: Ser[A] = implicitly[Ser[A]]

  implicit val byteBufSer: Ser[ByteBuf] = new Ser[ByteBuf] {
    override def contentType: AsciiString = HttpHeaderValues.APPLICATION_OCTET_STREAM

    override def serialize(a: ByteBuf, alloscator: ByteBufAllocator): ByteBuf = a
  }

  implicit val byteArraySer: Ser[Array[Byte]] = new Ser[Array[Byte]] {
    override def contentType: AsciiString = HttpHeaderValues.APPLICATION_OCTET_STREAM

    override def serialize(a: Array[Byte], allocator: ByteBufAllocator): ByteBuf =
      allocator.buffer(a.length).writeBytes(a)
  }

  implicit val stringSer: Ser[String] = Ser[Array[Byte]].contraMap(_.getBytes, HttpHeaderValues.TEXT_PLAIN)
}
