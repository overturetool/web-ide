name := """overture_webide"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  //javaJdbc,
  //cache,
  javaWs,
  filters,
  "org.apache.commons" % "commons-vfs2" % "2.0",
  "commons-io" % "commons-io" % "2.4",

  "com.google.oauth-client" % "google-oauth-client" % "1.21.0",
  "com.google.api-client" % "google-api-client" % "1.21.0",

//  "org.slf4j" % "slf4j-api" % "1.7.21",
//  "org.slf4j" % "slf4j-log4j12" % "1.7.21",

  "com.jayway.restassured" % "rest-assured" % "2.8.0" % "test",
  "com.jayway.restassured" % "scala-support" % "2.8.0" % "test",
  "com.jayway.restassured" % "json-path" % "2.8.0" % "test",
  "com.jayway.restassured" % "xml-path" % "2.8.0" % "test",
  "com.jayway.restassured" % "json-schema-validator" % "2.8.0" % "test",
  "org.hamcrest" % "hamcrest-all" % "1.3" % "test",

  "javax.websocket" % "javax.websocket-api" % "1.1" % "test",
  "org.glassfish.tyrus" % "tyrus-client" % "1.12" % "test",
  "org.glassfish.tyrus" % "tyrus-container-grizzly-client" % "1.12" % "test"
)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator


fork in run := true