name := "caja"

version := "1.0"

lazy val `caja` = (project in file(".")).enablePlugins(PlayScala)
val slick_version = "1.1.1"
val slick_codegen_version = "3.1.1"
val mariadb_version = "1.3.3"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  cache
, ws
, specs2 % Test
, evolutions
, "com.typesafe.slick" %% "slick-codegen"         % slick_codegen_version
, "com.typesafe.play"  %% "play-slick"            % slick_version
, "com.typesafe.play"  %% "play-slick-evolutions" % slick_version
, "org.mariadb.jdbc"   %  "mariadb-java-client"   % mariadb_version
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"  