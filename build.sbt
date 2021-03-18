val scala213 = "2.13.3"
val scala3 = "3.0.0-RC1"

ThisBuild / crossScalaVersions := Seq(scala213, scala3)
ThisBuild / scalaVersion := scala213

ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches +=
  RefPredicate.StartsWith(Ref.Tag("v"))

def withGithubPublish(project: Project): Project = {
  scala.util.Try(scala.sys.env("GITHUB_TOKEN")).toOption.map { token =>
    project.settings(
      githubOwner := "tomasherman",
      githubRepository := "netty4s"
    )
  } getOrElse {
    project.disablePlugins(GitHubPackagesPlugin)
  }
}
lazy val root = withGithubPublish(project)
  .in(file("."))
  .disablePlugins(GitHubPackagesPlugin)
  .settings(publish := false)
  .aggregate(examples, core)

lazy val examples = withGithubPublish(project)
  .in(file("examples"))
  .disablePlugins(GitHubPackagesPlugin)
  .dependsOn(core)
  .settings(publish := false)

lazy val core = withGithubPublish(project)
  .in(file("core"))
  .disablePlugins(GitHubPackagesPlugin)
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.Cats.effect,
      Dependencies.Netty.all
    )
  )

name := "netty4s"
organization := "tomasherman"
version := "1.0"
