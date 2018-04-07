import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.4",
      version := "0.1.0-SNAPSHOT"
    )),
    name := "tenable_challenge",
    libraryDependencies ++= Seq(
      scalaTest % Test,
      "com.typesafe.akka" %% "akka-http" % "10.1.1",
      "com.typesafe.akka" %% "akka-actor" % "2.5.11",
      "com.typesafe.akka" %% "akka-stream" % "2.5.11",
      "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.1",
      "org.scalikejdbc" %% "scalikejdbc" % "2.5.2",
      "com.h2database" % "h2" % "1.4.197",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "org.apache.logging.log4j" %% "log4j-api-scala" % "11.0",
      "org.apache.logging.log4j" % "log4j-api" % "2.11.0",
      "org.scalikejdbc" %% "scalikejdbc-test" % "3.2.2" % "test",
    )
  )
