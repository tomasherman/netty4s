package netty4s.core.server.api

trait HttpApp[F[_]] {
  def asRouter: Router[F]
}

object HttpApp {
  def fromRouter[F[_]](router: Router[F]): HttpApp[F] =
    new HttpApp[F] {
      override def asRouter: Router[F] = router
    }
}
