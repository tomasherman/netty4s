package netty4s.core.server.netty.utils

import cats.effect.{Async, Concurrent}
import io.netty.channel.Channel
import io.netty.util.concurrent.{Future, GenericFutureListener}
import wvlet.log.LogSupport

object FutureListeners {
  case object Cancelled extends Throwable("Underlying netty future was canceled")

  def uncancellable[F[_]: Async, A](unsafeFuture: => Future[A]): F[A] = {
    Async[F].async[A] { cb =>
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
    }
  }

  def cancellable[F[_]: Concurrent, A](unsafeFuture: => Future[A]): F[A] =
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

  def channelClosedLog(channel: Channel): GenericFutureListener[Future[Void]] =
    new GenericFutureListener[Future[Void]] with LogSupport {
      override def operationComplete(future: Future[Void]): Unit = {
        debug(s"channel $channel was closed")
      }
    }
}
