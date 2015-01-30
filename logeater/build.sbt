

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