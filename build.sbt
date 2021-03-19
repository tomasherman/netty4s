val scala213 = "2.13.3"
val scala3 = "3.0.0-RC1"

ThisBuild / crossScalaVersions := Seq(scala213, scala3)
ThisBuild / scalaVersion := scala213

ThisBuild / githubWorkflowPublishTargetBranches := Seq(
  // RefPredicate.Equals(Ref.Branch("master")) // disabled until scala 3 is out - publish fails with it
)

ThisBuild / githubWorkflowEnv := Map(
  "PACKAGE_WRITE_TOKEN" -> "${{ secrets.PACKAGE_WRITE_TOKEN }}"
)

def withGithubPublish(project: Project): Project = {
  scala.util.Try(scala.sys.env("PACKAGE_WRITE_TOKEN")).toOption.map { _ =>
    project
      .settings(
        githubOwner := "tomasherman",
        githubRepository := "netty4s",
        githubTokenSource := TokenSource.Environment("PACKAGE_WRITE_TOKEN")
      )
  } getOrElse {
    project.disablePlugins(GitHubPackagesPlugin)
  }
}
lazy val root = withGithubPublish(project)
  .in(file("."))
  .settings(publish := false)
  .aggregate(examples, core)

lazy val examples = withGithubPublish(project)
  .in(file("code/examples"))
  .dependsOn(core)
  .settings(
    publish := false,
    libraryDependencies ++= Seq(
      ExamplesDependencies.FS2.core
    )
  )

lazy val core = withGithubPublish(project)
  .in(file("code/core"))
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.Cats.effect,
      Dependencies.Netty.all
    )
  )

name := "netty4s"
organization := "tomasherman"
version := "1.0"
