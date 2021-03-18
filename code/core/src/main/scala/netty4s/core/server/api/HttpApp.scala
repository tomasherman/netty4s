package netty4s.core.server.api

trait HttpApp

object HttpApp {
  def fromRouter[F[_]](router: Router[F]): HttpApp = ???
}
