name := """play-showcase"""
organization := "com.letusfly85"

version := "1.0.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.3"

libraryDependencies ++= {
  Seq(
   guice,
   "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
  )
}

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.letusfly85.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.letusfly85.binders._"
