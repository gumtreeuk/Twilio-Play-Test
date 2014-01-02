name := "twilio-test"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  cache,
  "com.twilio.sdk" % "twilio-java-sdk" % "3.4.1"
)     

play.Project.playScalaSettings
