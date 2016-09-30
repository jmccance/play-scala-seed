//////////////
// Projects

lazy val common =
  (project in file("common"))
    .settings(commonSettings)

lazy val example =
  (project in file("example"))
    .dependsOn(common)
    .settings(commonSettings)

lazy val `example-web` =
  (project in file("example-web"))
    .enablePlugins(PlayScala)
    .disablePlugins(PlayLayoutPlugin)
    .dependsOn(common)
    .settings(commonSettings)

/////////////////////
// Global Settings

organization in ThisBuild := "com.example"
scalaVersion in ThisBuild := "2.11.8"

scalafmtConfig in ThisBuild := Some(file(".scalafmt"))

// Never publish this root project as an artifact; there's nothing of value here.
publishArtifact in ThisProject := false

//////////////
// Settings

lazy val commonSettings =
  (commonDependencies
    ++ compileSettings
    ++ testSettings)

lazy val commonDependencies = Seq(
  libraryDependencies += Dependencies.Scalactic.scalactic
)

lazy val compileSettings = Seq(
  scalacOptions in (Compile, compile) ++= Seq(
    "-Xfatal-warnings",
    "-Xfuture",
    "-Xlint",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature",
    "-target:jvm-1.8",
    "-unchecked"
  )
)

lazy val testSettings = Seq(
  libraryDependencies ++= Seq(
    Dependencies.ScalaCheck.scalacheck,
    Dependencies.ScalaTest.scalatest
  ).map(_ % Test)
)

////////////
// Extras

/* These settings are not applied to any project. If you'd like to enable any of
   them, just add the setting to either commonSettings (if you'd like it in all
   projects) or to the specific project you want. They can also safely be deleted
   if you'd like to clean up your build file. */

// Defines a target repository for publishing artifacts.
val publishSettings = Seq(
  publishTo := {
    def repo(path: String) =
      Some(
        s"ExampleCorp ${path.capitalize}"
          at s"https://example.com/nexus/content/repositories/$path")

    if (isSnapshot.value) {
      repo("snapshots")
    } else {
      repo("releases")
    }
  }
)

// Defines code coverage minimums that must be met. To run tests with code
// coverage, run `./bin/activator coverage test coverageReport`.
val scoverageSettings = Seq(
  coverageMinimum := 80,
  coverageFailOnMinimum := true
)
