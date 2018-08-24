resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"

resolvers += "bintray-sbt-plugin-releases" at "http://dl.bintray.com/content/sbt/sbt-plugin-releases"

// to deploy to bintray
addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.4")

// to format scala source code
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.2")

// enable updating file headers eg. for copyright
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.0.0")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.4")
