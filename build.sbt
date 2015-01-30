name := "LISAEndpoint"

scalaVersion := "2.10.3"

version := "0.5"


libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.2.3",
  "com.typesafe.akka" %% "akka-camel" % "2.2.3",
  "org.apache.activemq" % "activemq-camel" % "5.8.0",
  "com.typesafe.akka" %% "akka-testkit" % "2.2.3"
)

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.0" % "test"

libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "0.6.0"

resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/Releases"
  

libraryDependencies += "commons-lang" % "commons-lang" % "2.6"

lazy val root =
        project.in( file(".") )
   .aggregate(core, dbeater, elasticsearch, logeater, messagefold)

lazy val core = project

lazy val dbeater = project.dependsOn(core)

lazy val elasticsearch = project.dependsOn(core)

lazy val logeater = project.dependsOn(core)

lazy val messagefold = project.dependsOn(core)


