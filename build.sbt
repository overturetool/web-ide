name := """overture_webide"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  filters,
  "org.apache.commons" % "commons-vfs2" % "2.0",
  "commons-io" % "commons-io" % "2.4",
  "org.overturetool.core" % "interpreter" % "2.3.0",

  "com.jayway.restassured" % "rest-assured" % "2.8.0" % "test",
  "com.jayway.restassured" % "scala-support" % "2.8.0" % "test",
  "com.jayway.restassured" % "json-path" % "2.8.0",
  "com.jayway.restassured" % "xml-path" % "2.8.0",
  "com.jayway.restassured" % "json-schema-validator" % "2.8.0" % "test",
  "org.hamcrest" % "hamcrest-all" % "1.3"
)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator


fork in run := true