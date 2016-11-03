name := "akka-persistence-journal-writer"

version := "0.0.1"

organization := "com.github.dnvriend"

scalaVersion := "2.11.8"

libraryDependencies ++= {
  val akkaVersion = "2.4.12"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
    "com.typesafe.akka" %% "akka-persistence-query-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "com.github.dnvriend" %% "akka-persistence-inmemory" % "1.3.13" % Test,
    "commons-io" % "commons-io" % "2.5" % Test,
    "org.iq80.leveldb" % "leveldb" % "0.7" % Test,
    "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8" % Test,
    "org.scalatest" %% "scalatest" % "3.0.0" % Test
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
  .setPreference(DoubleIndentClassDeclaration, true)

// enable updating file headers //
import de.heikoseeberger.sbtheader.license.Apache2_0

headers := Map(
  "scala" -> Apache2_0("2016", "Dennis Vriend"),
  "conf" -> Apache2_0("2016", "Dennis Vriend", "#")
)

enablePlugins(AutomateHeaderPlugin)