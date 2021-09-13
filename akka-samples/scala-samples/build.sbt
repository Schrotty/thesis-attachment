name := "scala-samples"
version := "0.1"
scalaVersion := "2.13.2"

val AkkaVersion = "2.6.16"
libraryDependencies.++=(Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "org.scalatest" %% "scalatest" % "3.2.9" % Test,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test
))
