name := "akka-tcp"

version := "1.0"

scalaVersion := "2.12.4"

resolvers += "akka" at "http://repo.akka.io/snapshots"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.3"
)