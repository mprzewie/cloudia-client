name := "cloudia-client"

version := "1.0"

scalaVersion := "2.12.2"



resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.0",
  "com.typesafe.akka" %% "akka-remote" % "2.5.0"
//  ,"default" %% "cloudia-utils" % "1.0"
)