package netty4s.core.server.api.serde

import io.netty.buffer.{ByteBuf, ByteBufUtil}

trait DeSer[A] { self =>
  def deserialize(byteBuf: ByteBuf): A
  def map[B](f: A => B): DeSer[B] = (byteBuf: ByteBuf) => f(self.deserialize(byteBuf))
}

object DeSer {
  def apply[A: DeSer]: DeSer[A] = implicitly[DeSer[A]]

  implicit val byteArrayDeser: DeSer[Array[Byte]] = (byteBuf: ByteBuf) => ByteBufUtil.getBytes(byteBuf)

  implicit val stringDeser: DeSer[String] = DeSer[Array[Byte]].map(new String(_))
}
