//////////////
// Projects

lazy val common =
  (project in file("common"))
    .settings(
      commonSettings,
      libraryDependencies += Dependencies.Typesafe.Play.playJson % Provided
    )

lazy val contacts =
  (project in file("contacts")).dependsOn(common).settings(commonSettings)

lazy val `contacts-data` =
  (project in file("contacts-data"))
    .dependsOn(
      common,
      contacts
    )
    .settings(commonSettings)

lazy val `contacts-data-impl` =
  (project in file("contacts-data-impl"))
    .dependsOn(
      common,
      contacts,
      `contacts-data`
    )
    .settings(
      commonSettings,
      loggingDependencies
    )

lazy val `contacts-service` =
  (project in file("contacts-service"))
    .dependsOn(
      common,
      contacts,
      `contacts-data`
    )
    .settings(
      commonSettings
    )

lazy val `contacts-service-impl` =
  (project in file("contacts-service-impl"))
    .dependsOn(
      common,
      contacts,
      `contacts-data`,
      `contacts-service`
    )
    .settings(
      commonSettings,
      loggingDependencies
    )

lazy val `contacts-web` =
  (project in file("contacts-web"))
    .enablePlugins(PlayScala)
    .disablePlugins(PlayLayoutPlugin)
    .dependsOn(
      common,
      `contacts`,
      `contacts-data`,
      `contacts-data-impl`,
      `contacts-service`,
      `contacts-service-impl`
    )
    .settings(
      commonSettings,
      loggingDependencies,
      libraryDependencies ++= Seq(
        Dependencies.MacWire.macros
      )
    )

/////////////////////
// Global Settings

organization in ThisBuild := "com.example"
scalaVersion in ThisBuild := "2.11.8"

scalafmtConfig in ThisBuild := Some(file(".scalafmt.conf"))

// Never publish this root project as an artifact; there's nothing of value here.
publishArtifact in ThisProject := false

//////////////
// Settings

lazy val commonSettings =
  (commonDependencies
    ++ compileSettings
    ++ testSettings)

lazy val commonDependencies = Seq(
  libraryDependencies ++= Seq(
    Dependencies.Scalactic.scalactic,
    Dependencies.Typesafe.config
  )
)

lazy val loggingDependencies = Seq(
  libraryDependencies += Dependencies.Typesafe.scalaLogging
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
