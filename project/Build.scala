import sbt._
import Keys._
import play.Project._

import com.github.play2war.plugin._

object ApplicationBuild extends Build {

  val appName = "qr-code-generator-scala"
  val appVersion = "0.0.1-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    "org.webjars" %% "webjars-play" % "2.1.0-3",
    "org.webjars" % "jquery" % "1.10.2-1",
    "org.webjars" % "bootstrap" % "3.0.0",
    "org.webjars" % "prettify" % "4-Mar-2013",
    "org.webjars" % "html5shiv" % "3.6.2",
    "org.webjars" % "respond" % "1.3.0",

    "org.apache.directory.studio" % "org.apache.commons.codec" % "1.8",

    "net.glxn" % "qrgen" % "1.3")

  val main = play.Project(appName, appVersion, appDependencies)
    .settings(Play2WarPlugin.play2WarSettings: _*)
    .settings(
      // Add your own project settings here
      scalaVersion := "2.10.3",
      Play2WarKeys.servletVersion := "3.0")
}
