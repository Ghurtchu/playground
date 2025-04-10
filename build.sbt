ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.11"

lazy val root = (project in file("."))
  .settings(
    name := "ce-ref-practice"
  )

libraryDependencies += "org.typelevel" %% "cats-core" % "2.9.0"
libraryDependencies += "org.typelevel" %% "cats-effect" % "3.4.8"
libraryDependencies += "co.fs2" %% "fs2-core" % "3.10.1"

libraryDependencies ++= Seq(
  "com.softwaremill.sttp.client3" %% "core"              % "3.10.1",       // core STTP client
  "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats"     % "3.9.0",
)
