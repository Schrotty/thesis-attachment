name := "priceIRC-redux"
version := "1.0"
scalaVersion := "2.13.3"
maintainer := "rubenmaurer@live.de"
libraryDependencies ++= Seq(

  //ANTLR
  "org.antlr" % "ST4" % "4.3.1",
  "org.antlr" % "antlr4-runtime" % "4.9.2",
  "org.antlr" % "stringtemplate" % "4.0.2",

  //AKKA
  "com.typesafe.akka" %% "akka-actor-typed" % "2.6.16",
  "com.typesafe.akka" %% "akka-stream" % "2.6.16",

  "org.scalactic" %% "scalactic" % "3.2.9",
  "org.scalactic" %% "scalactic" % "3.2.9" % "test",
  "org.scalatest" % "scalatest_2.13" % "3.2.9",
  "org.scalatestplus" %% "scalacheck-1-15" % "3.2.9.0" % "test",
  "commons-lang" % "commons-lang" % "20030203.000129",
  "com.lihaoyi" %% "os-lib" % "0.7.8",

  //LOGGING
  "ch.qos.logback" % "logback-classic" % "1.2.5",
  "org.apache.logging.log4j" %% "log4j-api-scala" % "12.0",
  "org.apache.logging.log4j" % "log4j-core" % "2.14.1" % Runtime,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",

  "org.fusesource.jansi" % "jansi" % "2.3.4",

  "org.scala-lang.modules" %% "scala-async" % "1.0.0",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided
)

// PLUGINS AND SETTINGS
enablePlugins(Antlr4Plugin)
Antlr4 / antlr4PackageName := Some("de.rubenmaurer.price.antlr4")
Antlr4 / antlr4Version := "4.9.2"

enablePlugins(JavaAppPackaging)
enablePlugins(UniversalPlugin)

inThisBuild(
  List(
    scalaVersion := scalaVersion.value,
    semanticdbEnabled := true, // enable SemanticDB
    semanticdbVersion := scalafixSemanticdb.revision, // use Scalafix compatible version
    scalafixScalaBinaryVersion := "2.13"
  )
)

lazy val root = (project in file(".")).
  enablePlugins(BuildInfoPlugin).
  settings(buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, buildInfoBuildNumber),
    buildInfoPackage := "de.rubenmaurer.price.util"
  )