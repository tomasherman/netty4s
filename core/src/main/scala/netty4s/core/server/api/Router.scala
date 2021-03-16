package netty4s.core.server.api

import netty4s.core.model.HttpRequest

import java.util

trait Router[F[_]] {
  def lookup(request: HttpRequest): Handler[F]
  def addAll(routesToAdd: List[Route[F]]): Router[F]
}

class SimpleRouter[F[_]] extends Router[F] {
  private[this] val routes: java.util.List[Route[F]] = new util.ArrayList[Route[F]]()

  override def lookup(request: HttpRequest): Handler[F] = ???

  override def addAll(routesToAdd: List[Route[F]]): Router[F] = {
    routesToAdd.foreach(routes.add)
    this
  }

}

object Router {
  def simple[F[_]]: Router[F] = new SimpleRouter[F]
  def simple[F[_]](routes: List[Route[F]]): Router[F] = {
    val r = simple[F]
    r.addAll(routes)
  }
  def simple[F[_]](routes: Route[F]*): Router[F] = {
    val r = simple[F]
    r.addAll(routes.toList)
  }
}