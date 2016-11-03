// enable publishing to jcenter
homepage := Some(url("https://github.com/dnvriend/akka-persistence-journal-writer"))

pomIncludeRepository := (_ => false)

pomExtra := <scm>
  <url>https://github.com/dnvriend/akka-persistence-journal-writer</url>
  <connection>scm:git@github.com:dnvriend/akka-persistence-journal-writer.git</connection>
  </scm>
  <developers>
    <developer>
      <id>dnvriend</id>
      <name>Dennis Vriend</name>
      <url>https://github.com/dnvriend</url>
    </developer>
  </developers>

publishMavenStyle := true

bintrayPackageLabels := Seq("akka", "persistence", "journal", "writer")

bintrayPackageAttributes ~=
  (_ ++ Map(
    "website_url" -> Seq(bintry.Attr.String("https://github.com/dnvriend/akka-persistence-journal-writer")),
    "github_repo" -> Seq(bintry.Attr.String("https://github.com/dnvriend/akka-persistence-journal-writer.git")),
    "issue_tracker_url" -> Seq(bintry.Attr.String("https://github.com/dnvriend/akka-persistence-journal-writer.git/issues/"))
  )
)