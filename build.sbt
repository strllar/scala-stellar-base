
name := "scala-stellar-base"

organization := "org.strllar"

scalaVersion := "2.12.3"

lazy val scalaxdrJVM = ProjectRef(uri("git://github.com/strllar/scala-xdr.git#master"), "xdrbaseJVM")
lazy val scalaxdrJS = ProjectRef(uri("git://github.com/strllar/scala-xdr.git#master"), "xdrbaseJS")

lazy val stellarbase = crossProject.in(file(".")).
  settings(
    name := "scala-stellar-base",
    organization := "org.strllar",
    version := "0.1-SNAPSHOT",

    scalaVersion := "2.12.3",

    unmanagedSourceDirectories in Compile += (baseDirectory.value.getParentFile / "shared" / "deps" / "nacl4s" / "src" / "main"),
    excludeFilter in unmanagedSources := HiddenFileFilter || new SimpleFileFilter(_.getParent.endsWith((new File("/com/emstlk/nacl4s/benchmark")).getPath))
  ).
  jvmSettings(
    // Add JVM-specific settings here
    libraryDependencies += "commons-codec" % "commons-codec" % "1.10",
    libraryDependencies += "org.bouncycastle" % "bcprov-jdk15on" % "1.57",
    libraryDependencies += "com.h2database" % "h2" % "1.4.196",

    resolvers += "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/maven-snapshots/",
    resolvers += Resolver.url("Typesafe Ivy Snapshots Repository", url("http://repo.typesafe.com/typesafe/ivy-snapshots"))(Resolver.ivyStylePatterns),
    libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.19",
    
    libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.0.9",

    libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.9",

    libraryDependencies += "com.typesafe.slick" %% "slick" % "3.2.1",

    libraryDependencies += "org.scodec" %% "scodec-core" % "1.10.3",

    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.3" % "test"
  ).
  jsSettings(
    // Add JS-specific settings here
    //scalaJSStage in Global := FullOptStage,
    //postLinkJSEnv := PhantomJSEnv().value,

    libraryDependencies += "org.scodec" %%% "scodec-core" % "1.10.3",

    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.3" % "test"
  )

lazy val targetJVM = stellarbase.jvm.dependsOn(scalaxdrJVM)
lazy val targetJS = stellarbase.js.dependsOn(scalaxdrJS)
