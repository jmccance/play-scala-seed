import sbt._

object Dependencies {
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

    object Play {
      val Version = "2.5.8"

      val playJson = "com.typesafe.play" %% "play-json" % Version
    }
  }
}
