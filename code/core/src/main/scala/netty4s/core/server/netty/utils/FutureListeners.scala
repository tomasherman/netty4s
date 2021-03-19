package netty4s.core.server.netty.utils

import cats.effect.Concurrent
import io.netty.util.concurrent.Future

object FutureListeners {
  case object Cancelled extends Throwable("Underlying netty future was canceled")

  def toF[F[_]: Concurrent, A](unsafeFuture: => Future[A]): F[A] =
    Concurrent[F].cancelable[A] { cb =>
      val future = unsafeFuture
      future.addListener { (f: Future[A]) =>
        if (f.isSuccess) {
          cb(Right(f.getNow))
        } else {
          if (f.isCancelled) {
            cb(Left(Cancelled))
          } else {
            cb(Left(f.cause()))
          }
        }
      }
      Concurrent[F].delay { val _ = future.cancel(true) }
    }
}