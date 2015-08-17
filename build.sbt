
name := "scala-stellar-base"

organization := "org.strllar"

lazy val root = project.in(file(".")).
  aggregate(targetJS, targetJVM).
  settings(
    publish := {},
    publishLocal := {}
  )

////temp scaffold for scalaxdr developing
lazy val scalaxdr = crossProject.crossType(CrossType.Pure).in(file("shared/deps/scala-xdr")).
  settings(
    libraryDependencies +=  "org.scala-lang" % "scala-reflect" % "2.10.5"
  )
lazy val scalaxdrjs = scalaxdr.js
lazy val scalaxdrjvm = scalaxdr.jvm
////temp end

lazy val stellarbase = crossProject.in(file(".")).
  dependsOn(scalaxdr % "compile").
  settings(
    name := "scala-stellar-base",
    organization := "org.strllar",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.10.5",

    resolvers += "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/maven-snapshots/",
    resolvers += Resolver.url("Typesafe Ivy Snapshots Repository", url("http://repo.typesafe.com/typesafe/ivy-snapshots"))(Resolver.ivyStylePatterns),
    libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.12",
    libraryDependencies += "com.typesafe.akka" %% "akka-stream-experimental" % "1.0",
    libraryDependencies += "com.typesafe.akka" %% "akka-http-core-experimental" % "1.0",
    libraryDependencies += "com.typesafe.akka" %% "akka-http-experimental" % "1.0",
    libraryDependencies += "com.typesafe.slick" %% "slick" % "3.0.0"
  ).
  jvmSettings(
    // Add JVM-specific settings here
    libraryDependencies += "commons-codec" % "commons-codec" % "1.10",
    libraryDependencies += "org.bouncycastle" % "bcprov-jdk15on" % "1.52",
    libraryDependencies += "com.h2database" % "h2" % "1.4.186",
    unmanagedSourceDirectories in Compile += baseDirectory.value / "deps" / "tweetnacl-java" / "src",
    excludeFilter in unmanagedSources := HiddenFileFilter || "TweetNaclFast.java" || new SimpleFileFilter(_.getParent.endsWith((new File("iwebpp/crypto/tests")).getPath)),
    //for Tests
    resolvers += Resolver.url("inthenow-releases", url("http://dl.bintray.com/inthenow/releases"))(Resolver.ivyStylePatterns),
    libraryDependencies += "com.github.inthenow" %% "zcheck" % "0.6.2",
    //libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.12.1" % "test"
    testOptions in Test += Tests.Argument(TestFrameworks.ScalaCheck)
  ).
  jsSettings(
    // Add JS-specific settings here
  )

lazy val targetJVM = stellarbase.jvm
lazy val targetJS = stellarbase.js
