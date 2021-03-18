package netty4s.core.server.api.requestmatch

import io.netty.handler.codec.http.HttpMethod
import netty4s.core.model.HttpRequest
import netty4s.core.model.JTypes.JHttpMethod

sealed trait RequestMatcher {
  def isMatch(request: HttpRequest): Boolean
}

sealed trait PathMatcher extends RequestMatcher

class SimplePathMatch(blueprint: String) extends PathMatcher {
  override def isMatch(request: HttpRequest): Boolean = request.uri == blueprint
}

class MethodMatcher(blueprint: JHttpMethod) extends RequestMatcher {
  def ->(pathMatcher: PathMatcher): RequestMatcher = {
    new MethodAndPathMatcher(this, pathMatcher)
  }

  def ->(rawPath: String): RequestMatcher = {
    new MethodAndPathMatcher(this, new SimplePathMatch(rawPath))
  }

  override def isMatch(request: HttpRequest): Boolean =
    request.method == blueprint
}

class MethodAndPathMatcher(
    methodMatcher: MethodMatcher,
    pathMatcher: PathMatcher
) extends RequestMatcher {
  override def isMatch(request: HttpRequest): Boolean =
    methodMatcher.isMatch(request) && pathMatcher.isMatch(request)
}

object MatchAny extends RequestMatcher {
  override def isMatch(request: HttpRequest): Boolean = true
}

object MethodMatcher {
  case object Get extends MethodMatcher(HttpMethod.GET)
  case object Post extends MethodMatcher(HttpMethod.POST)
  case object Put extends MethodMatcher(HttpMethod.PUT)
}

object PathMatcher {
  def matchPath(blueprint: String): PathMatcher = new SimplePathMatch(blueprint)
}
