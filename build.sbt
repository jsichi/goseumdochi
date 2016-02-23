name := "goseumdochi"

organization := "org.lingeringsocket"

val javacppVersion = "0.11"

version := javacppVersion

scalaVersion := "2.11.7"

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-Xlint")

lazy val platform = org.bytedeco.javacpp.Loader.getPlatform

classpathTypes += "maven-plugin"

libraryDependencies ++= Seq(
  "com.typesafe.akka"      %% "akka-actor"     % "2.4.1",
  "com.typesafe.akka"      %% "akka-testkit"     % "2.4.1" % "test",
  "org.bytedeco"                 % "javacpp"         % javacppVersion,
  "org.bytedeco"                 % "javacv"          % javacppVersion,
  "org.bytedeco.javacpp-presets" % "opencv" % ("2.4.11-" + javacppVersion) classifier "",
  "org.bytedeco.javacpp-presets" % "opencv" % ("2.4.11-" + javacppVersion) classifier platform,
  "org.specs2"        %% "specs2"             % "2.4.3"           % "test"
)

autoCompilerPlugins := true

parallelExecution in Test := false

fork := true

javaOptions += "-Xmx1G"

maxErrors := 99

traceLevel := 10

mainClass in Compile := Some("goseumdochi.sphero.SpheroMain")