name := "akka-redis-websockets-play-scala_translation"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "com.typesafe" %% "play-plugins-redis" % "2.1.1"
)

resolvers += "org.sedis" at "http://pk11-scratch.googlecode.com/svn/trunk"

scalacOptions ++= Seq( "-deprecation", "-unchecked", "-feature" )

play.Project.playScalaSettings