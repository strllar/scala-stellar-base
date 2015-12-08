
name := "scala-stellar-base"

organization := "org.strllar"

scalaVersion := "2.10.5"

val xdrGenTask = TaskKey[Unit]("xdrGen", "Generate sources from stellar xdr files")
lazy val root = project.in(file(".")).
  aggregate(targetJS, targetJVM).
  settings(
    publish := {},
    publishLocal := {},
    xdrGenTask := {
      println("Generating sources from stellar xdr files...")
      org.strllar.scalaxdr.XDRParser.main(Array.empty[String])
      println("...done.")
    }
  )

lazy val scalaxdrJVM = ProjectRef(file("shared/deps/scala-xdr"), "xdrbaseJVM")
lazy val scalaxdrJS = ProjectRef(file("shared/deps/scala-xdr"), "xdrbaseJS")


lazy val stellarbase = crossProject.in(file(".")).
  settings(
    name := "scala-stellar-base",
    organization := "org.strllar",
    version := "0.1-SNAPSHOT",

    scalaVersion := "2.10.5",
    scalacOptions  += "-Yfundep-materialization", //fix for SI-7470 where Generic.Aux[T, L] doesn't work

    unmanagedSourceDirectories in Compile <+= (baseDirectory)(_.getParentFile / "shared" / "deps" / "nacl4s" / "src" / "main"),
    excludeFilter in unmanagedSources := HiddenFileFilter || new SimpleFileFilter(_.getParent.endsWith((new File("/com/emstlk/nacl4s/benchmark")).getPath))
  ).
  jvmSettings(
    // Add JVM-specific settings here
    libraryDependencies += "commons-codec" % "commons-codec" % "1.10",
    libraryDependencies += "org.bouncycastle" % "bcprov-jdk15on" % "1.52",
    libraryDependencies += "com.h2database" % "h2" % "1.4.186",

    resolvers += "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/maven-snapshots/",
    resolvers += Resolver.url("Typesafe Ivy Snapshots Repository", url("http://repo.typesafe.com/typesafe/ivy-snapshots"))(Resolver.ivyStylePatterns),
    libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.12",
    libraryDependencies += "com.typesafe.akka" %% "akka-stream-experimental" % "1.0",
    libraryDependencies += "com.typesafe.akka" %% "akka-http-core-experimental" % "1.0",
    libraryDependencies += "com.typesafe.akka" %% "akka-http-experimental" % "1.0",
    libraryDependencies += "com.typesafe.slick" %% "slick" % "3.0.0",

    libraryDependencies += "org.scodec" %% "scodec-core" % "1.8.3",

      //for Tests
    resolvers += Resolver.url("inthenow-releases", url("http://dl.bintray.com/inthenow/releases"))(Resolver.ivyStylePatterns),
    libraryDependencies += "com.github.inthenow" %% "zcheck" % "0.6.2",
    //libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.12.1" % "test"
    testOptions in Test += Tests.Argument(TestFrameworks.ScalaCheck)
  ).
  jsSettings(
    // Add JS-specific settings here
    //scalaJSStage in Global := FullOptStage,
    //postLinkJSEnv := PhantomJSEnv().value,

    libraryDependencies += "org.scodec" %%% "scodec-core" % "1.8.3",

    resolvers += Resolver.url("inthenow-releases", url("http://dl.bintray.com/inthenow/releases"))(Resolver.ivyStylePatterns),
    libraryDependencies += "com.github.inthenow" %%% "zcheck" % "0.6.2",
    //libraryDependencies += "org.scalacheck" %%% "scalacheck" % "1.12.1" % "test"
    testFrameworks += new TestFramework("org.scalacheck.ScalaCheckFramework")
    //ScalaJSKeys.scalaJSTestFramework := "org.scalacheck.ScalaCheckFramework"
  )

lazy val targetJVM = stellarbase.jvm.dependsOn(scalaxdrJVM)
lazy val targetJS = stellarbase.js.dependsOn(scalaxdrJS)
