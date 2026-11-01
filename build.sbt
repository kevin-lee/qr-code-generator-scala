ThisBuild / scalaVersion := "2.13.18"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, DebianPlugin, SystemdPlugin)
  .settings(
    name    := "qr-code-generator-scala",
    version := "0.0.2-SNAPSHOT",
    resolvers += "jitpack" at "https://jitpack.io",
    libraryDependencies ++= Seq(
      guice,
      "org.webjars"              %% "webjars-play"  % "3.0.10",
      "org.webjars"               % "jquery"        % "3.7.1",
      "org.webjars"               % "bootstrap"     % "3.4.1",
      "org.webjars"               % "prettify"      % "4-Mar-2013-1",
      "org.webjars"               % "html5shiv"     % "3.7.3-1",
      "org.webjars"               % "respond"       % "1.4.2-1",
      "commons-codec"             % "commons-codec" % "1.23.0",
      "com.github.kenglxn.qrgen"  % "javase"        % "3.0.1",
      "qa.hedgehog"              %% "hedgehog-sbt"  % "0.15.1" % Test,
    ),
    testFrameworks += TestFramework("hedgehog.sbt.Framework"),
    TwirlKeys.templateImports += "models._",
    Assets / LessKeys.less / includeFilter := "*.less",
    LessKeys.compress         := true,
    Assets / pipelineStages   := Seq(uglify),
    scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint", "-Wconf:src=routes/.*:s,src=twirl/.*:s"),
    Linux / maintainer        := "Kevin Lee <kevin@kevinlee.io>",
    Linux / packageSummary    := "QR Code Generator",
    packageDescription        := "QR Code Generator app",
    debianPackageDependencies := Seq("openjdk-21-jre-headless"),
    Debian / version          := sys.env.get("BUILD_NUMBER").fold(version.value)(n => s"${version.value}+$n"),
    Universal / javaOptions ++= Seq(
      "-J-Xmx192m",
      "-Dhttp.address=127.0.0.1",
      "-Dpidfile.path=/dev/null",
      "-Djava.awt.headless=true",
      "-Dplay.http.context=/qr-code-generator-scala",
    ),
  )
