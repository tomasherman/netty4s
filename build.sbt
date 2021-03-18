val scala213 = "2.13.3"
val scala3 = "3.0.0-RC1"

ThisBuild / crossScalaVersions := Seq(scala213, scala3)
ThisBuild / scalaVersion := scala213
ThisBuild / githubWorkflowPublishTargetBranches := Seq(
  RefPredicate.Equals(Ref.Branch("master"))
)
ThisBuild / githubWorkflowEnv := Map(
  "PACKAGE_WRITE_TOKEN" -> "PACKAGE_WRITE_TOKEN"
)

def withGithubPublish(project: Project): Project = {
  scala.util.Try(scala.sys.env("PACKAGE_WRITE_TOKEN")).toOption.map { token =>
    println(s"token: $token")
    project
      .settings(
        githubOwner := "tomasherman",
        githubRepository := "netty4s",
        githubTokenSource := TokenSource.Environment("PACKAGE_WRITE_TOKEN")
      )
      .enablePlugins(GitHubPackagesPlugin)
  } getOrElse {
    project.disablePlugins(GitHubPackagesPlugin)
  }
}
lazy val root = withGithubPublish(project)
  .in(file("."))
  .settings(publish := false)
  .aggregate(examples, core)

lazy val examples = withGithubPublish(project)
  .in(file("examples"))
  .dependsOn(core)
  .settings(publish := false)

lazy val core = withGithubPublish(project)
  .in(file("core"))
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.Cats.effect,
      Dependencies.Netty.all
    )
  )

name := "netty4s"
organization := "tomasherman"
version := "1.0"
