import sbt._

object ExamplesDependencies {
  object FS2 {
    val core = "co.fs2" %% "fs2-core" % "2.5.3"
  }

  object Logging {
    val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
  }
}
