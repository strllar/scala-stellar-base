
name := "scala-stellar-base"

organization := "org.strllar"

scalaVersion := "2.10.5"

lazy val root = project.in(file(".")).
  aggregate(targetJS, targetJVM).
  settings(
    publish := {},
    publishLocal := {}
  )

////temp scaffold for scalaxdr developing
lazy val scalaxdr = crossProject.crossType(CrossType.Pure).in(file("shared/deps/scala-xdr")).
  settings(
    scalaVersion := "2.10.5",
    libraryDependencies ++=  Seq("org.scala-lang" % "scala-reflect" % "2.10.5",
      "org.scalamacros" %% "quasiquotes" % "2.1.0",
      compilerPlugin("org.scalamacros" % s"paradise" % "2.1.0" cross CrossVersion.full)
    )
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

lazy val targetJVM = stellarbase.jvm
lazy val targetJS = stellarbase.js
