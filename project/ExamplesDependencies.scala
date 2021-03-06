import sbt._

object ExamplesDependencies {
  object FS2 {
    val core = "co.fs2" %% "fs2-core" % "2.5.3"
  }

  object Circe {
    val circeVersion = "0.14.0-M4"
    val generic = "io.circe" %% "circe-generic" % circeVersion
  }

}
