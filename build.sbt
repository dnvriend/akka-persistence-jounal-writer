import sbt._
import sbt.Keys._

name := "akka-persistence-journal-writer"

organization := "com.github.dnvriend"

scalaVersion := "2.12.6"

crossScalaVersions := Seq("2.11.8", "2.12.6")

resolvers += Resolver.jcenterRepo
externalResolvers ++= Seq(
  "dnvriend" at "https://dl.bintray.com/dnvriend/maven"
)

libraryDependencies ++= {
  val akkaVersion = "2.5.15"
  val akkaPersistenceInMemoryVersion = "2.5.1.2"
  val commonIoVersion = "2.6"
  val leveldbVersion = "0.10"
  val leveldbJniVersion = "1.8"
  val scalaTestVersion = "3.0.5"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
    "com.typesafe.akka" %% "akka-persistence-query" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "com.github.dnvriend" %% "akka-persistence-inmemory" % akkaPersistenceInMemoryVersion % Test,
    "commons-io" % "commons-io" % commonIoVersion % Test,
    "org.iq80.leveldb" % "leveldb" % leveldbVersion % Test,
    "org.fusesource.leveldbjni" % "leveldbjni-all" % leveldbJniVersion % Test,
    "org.scalatest" %% "scalatest" % scalaTestVersion % Test
  )
}

fork in Test := true

parallelExecution in Test := false

licenses += ("Apache-2.0", url("http://opensource.org/licenses/apache2.0.php"))

// enable scala code formatting //
import com.typesafe.sbt.SbtScalariform

import scalariform.formatter.preferences._

// Scalariform settings
SbtScalariform.autoImport.scalariformPreferences := SbtScalariform.autoImport.scalariformPreferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(DoubleIndentConstructorArguments, true)

// enable updating file headers //

import de.heikoseeberger.sbtheader._
headerLicense := Some(HeaderLicense.ALv2("2016", "Dennis Vriend"))

enablePlugins(AutomateHeaderPlugin)