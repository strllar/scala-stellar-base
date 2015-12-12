lazy val plugins = project.in(file(".")).settings(
  addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.5")
)
