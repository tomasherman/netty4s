val scala213 = "2.13.3"
val scala3 = "3.0.0-RC1"

ThisBuild / crossScalaVersions := Seq(scala213, scala3)
ThisBuild / scalaVersion := scala213

ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches +=
  RefPredicate.StartsWith(Ref.Tag("v"))

val commonSettings = Seq(
)

lazy val root = project
  .in(file("."))
  .settings(publish := false)
  .settings(commonSettings)
  .aggregate(examples, core)

lazy val examples = project
  .in(file("examples"))
  .dependsOn(core)
  .settings(publish := false)
  .settings(commonSettings)

lazy val core = project
  .in(file("core"))
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.Cats.effect,
      Dependencies.Netty.all
    )
  )
  .settings(commonSettings)

name := "netty4s"
organization := "tomasherman"
version := "1.0"
