name := "LISAEndpoint"

lazy val depend = Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4-M1",
  "com.typesafe.akka" %% "akka-testkit" % "2.4-M1",
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test",
  "com.github.nscala-time" %% "nscala-time" % "2.0.0",
  "org.json4s" %% "json4s-native" % "3.2.11",
  "org.json4s" %% "json4s-ext" % "3.2.11",
  "org.slf4j" % "slf4j-simple" % "1.7.7"
)

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"

libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "2.0.0"

libraryDependencies += "org.json4s" %% "json4s-native" % "3.2.11"

resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/Releases"

lazy val commonSettings = Seq(
  version := "1.0.0",
  scalaVersion := "2.11.6",
  resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/Releases"
)






lazy val root = project.in( file(".") )
   .aggregate(core, dbeater, elasticsearch, logeater, messagefold)

lazy val core = project.
  settings(commonSettings: _*).
  settings(libraryDependencies ++= depend)

lazy val dbeater = project.dependsOn(core).
  settings(commonSettings: _*).
  settings(libraryDependencies ++= depend)

lazy val elasticsearch = project.dependsOn(core).
  settings(commonSettings: _*).
  settings(libraryDependencies ++= depend)

lazy val logeater = project.dependsOn(core).
  settings(commonSettings: _*).
  settings(libraryDependencies ++= depend)

lazy val messagefold = project.dependsOn(core).
  settings(commonSettings: _*).
  settings(libraryDependencies ++= depend)


