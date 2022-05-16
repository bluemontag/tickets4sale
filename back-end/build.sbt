name := "tickets4sale"
description := "Ticket sales application"

scalaVersion := "2.13.8"

val javaVersion = "11"

val ZioVersion        = "2.0.0-RC1"
val ZioJsonVersion    = "0.3.0-RC2"
val ZioHttpVersion    = "2.0.0-RC2"

libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % ZioVersion,
  "dev.zio" %% "zio-json" % ZioJsonVersion,
  "io.d11" %% "zhttp" % ZioHttpVersion,
  "io.d11" %% "zhttp-test" % ZioHttpVersion % Test,
  "org.scalatest" %% "scalatest" % "3.2.10" % Test,
  "org.mockito" %% "mockito-scala-scalatest" % "1.17.5" % Test,
  "org.mockito" % "mockito-inline" % "3.11.2" % Test,
)