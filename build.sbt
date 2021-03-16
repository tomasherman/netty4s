
// The simplest possible sbt build file is just one line:

val scala213 = "2.13.3"
val scala3 = "3.0.0-RC2"
ThisBuild / crossScalaVersions := Seq(scala213, scala3)
ThisBuild / scalaVersion := scala213

name := "hello-world"
organization := "ch.epfl.scala"
version := "1.0"


libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"


ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches +=
  RefPredicate.StartsWith(Ref.Tag("v"))
