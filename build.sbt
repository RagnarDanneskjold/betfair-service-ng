name := "betfair-service-ng"

organization := "betfair"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.3"

scalacOptions := Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xlint",
  "-language:reflectiveCalls",
  "-Xmax-classfile-name", "128"
)

resolvers ++= Seq(
  "spray repo" at "http://repo.spray.io/"
)

// sbt-Revolver allows the running of the spray service in sbt in the background using re-start
seq(Revolver.settings: _*)

mainClass in (Compile,run) := Some("com.betfair.service.Boot")

packageOptions in (Compile, packageBin) +=
  Package.ManifestAttributes( java.util.jar.Attributes.Name.MAIN_CLASS -> "com.betfair.service.Boot" )

libraryDependencies ++= {
  val akkaV = "2.3.3"
  val sprayV = "1.3.1"
  Seq(
    "com.github.tomakehurst" % "wiremock" % "1.46" % "test",
    "io.spray" % "spray-can" % sprayV,
    "io.spray" % "spray-caching" % sprayV,
    "com.typesafe.akka" %% "akka-slf4j" % akkaV,
    "io.spray" % "spray-client" % sprayV,
    "io.spray" % "spray-routing" % sprayV,
    "io.spray" % "spray-testkit" % sprayV,
    "io.spray" %% "spray-json" % "1.2.5",
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "ch.qos.logback" % "logback-classic" % "1.1.0",
    "org.scalatest" % "scalatest_2.10" % "2.0" % "test",
    "org.mockito" % "mockito-all" % "1.9.5" % "test",
    "com.github.nscala-time" %% "nscala-time" % "0.8.0",
    "com.typesafe.play" %% "play-json" % "2.3.7",
    "org.apache.httpcomponents" % "httpclient" % "4.3.6"
  )
}

credentials += Credentials(Path.userHome / ".sbt/.credentials")