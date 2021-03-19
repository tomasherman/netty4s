package netty4s.core.server.api

import cats.effect.{Async, Concurrent, Effect}

import scala.concurrent.Future

trait Executor[F[_]] {
  def fireAndForget[A](fa: F[A]): Unit
  def runToFuture[A](fa: F[A]): Future[A]
}

object Executor {
  def catsEffect[F[_]: Effect]: Executor[F] =
    new Executor[F] {
      override def fireAndForget[A](fa: F[A]): Unit = {
        Effect[F].toIO(fa).unsafeRunAsyncAndForget()
      }

      override def runToFuture[A](fa: F[A]): Future[A] =
        Effect[F].toIO(fa).unsafeToFuture()
    }
}
