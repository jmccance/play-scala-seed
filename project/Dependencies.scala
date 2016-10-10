import sbt._

object Dependencies {
  object MacWire {
    // Macros are only needed at compile time.
    val macros = "com.softwaremill.macwire" %% "macros" % "2.2.4" % "provided"
  }

  object ScalaCheck {
    val scalacheck = "org.scalacheck" %% "scalacheck" % "1.13.2"
  }

  object Scalactic {
    val scalactic = "org.scalactic" %% "scalactic" % "3.0.0"
  }

  object ScalaTest {
    val scalatest = "org.scalatest" %% "scalatest" % "3.0.0"
  }

  object Typesafe {
    val config = "com.typesafe" % "config" % "1.3.1"

    val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"

    object Play {
      val Version = "2.5.8"

      val playJson = "com.typesafe.play" %% "play-json" % Version
    }
  }
}
