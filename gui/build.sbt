
libraryDependencies ++= Seq(
  "io.spray" %% "spray-can" % "1.3.3",
  "io.spray" %% "spray-routing" % "1.3.3",
  "io.spray" %% "spray-testkit" % "1.3.3" % "test"
)


libraryDependencies += "wabisabi" %% "wabisabi" % "2.1.3"

resolvers += "gphat" at "https://raw.github.com/gphat/mvn-repo/master/releases/"