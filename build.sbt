name := "akka-tcp"

version := "1.0.0"

scalaVersion := "2.12.4"
cancelable in Global := true

resolvers += "akka" at "http://repo.akka.io/snapshots"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.9",
  "com.typesafe.akka" %% "akka-http" % "10.1.0-RC1",
  "com.typesafe.akka" %% "akka-stream" % "2.5.9",
  "com.typesafe.play" %% "play-json" % "2.6.7",
  "com.chuusai" %% "shapeless" % "2.3.3"
)