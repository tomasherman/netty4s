package netty4s.core.server.api.requestmatch

trait RequestMatcherBuilder {
  def toMatcher: RequestMatcher
}
