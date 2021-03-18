package netty4s.core.server.api

trait Executor[F[_]] {
  def run()
}