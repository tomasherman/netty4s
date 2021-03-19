import sbt._

object Dependencies {
  object Netty {
    val all = "io.netty" % "netty-all" % "4.1.60.Final"
  }
  object Cats {
    val effect = "org.typelevel" %% "cats-effect" % "2.3.3"
  }
}
