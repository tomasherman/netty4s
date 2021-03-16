val scala213 = "2.13.3"
val scala3 = "3.0.0-RC1"

ThisBuild / crossScalaVersions := Seq(scala213, scala3)
ThisBuild / scalaVersion := scala213

ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches +=
  RefPredicate.StartsWith(Ref.Tag("v"))

lazy val root = project.in(file("."))
                       .dependsOn(examples)
                       .dependsOn(core)

lazy val examples = project.in(file("examples"))
                           .dependsOn(core)

lazy val core = project.in(file("core")).settings(
  libraryDependencies ++= Seq(
    Dependencies.Cats.effect,
    Dependencies.Netty.all,
    Dependencies.Other.newtype,
  )
)

name := "hello-world"
organization := "ch.epfl.scala"
version := "1.0"
