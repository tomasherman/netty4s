import sbt._

object Dependencies {
  object Netty {
    val all = "io.netty" % "netty-all" % "4.1.60.Final"
  }
  object Cats {
    val effect = "org.typelevel" %% "cats-effect" % "2.3.3"
  }
  object Logging {
    val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2" // no release for scala3 >:(
    val airframeLog = "org.wvlet.airframe" %% "airframe-log" % "21.3.0"
  }
  object Circe {
    val circeVersion = "0.12.3"
    val core = "io.circe" %% "circe-core" % circeVersion
  }
}
