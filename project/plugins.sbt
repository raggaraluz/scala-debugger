logLevel := Level.Warn

resolvers += Resolver.sonatypeRepo("snapshots")

resolvers += Resolver.sonatypeRepo("releases")

{
  val v = VersionNumber(sys.props("java.specification.version"))

  // If JDK 8 or lower
  if (v._1.exists(_ == 1) && v._2.exists(_ < 9)) {
    Seq(addSbtPlugin("org.scala-debugger" % "sbt-jdi-tools" % "1.0.0"))
  } else {
    Seq()
  }
}

// Used for building fat jars
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.1")

// Used for better dependency resolution and downloading
addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.0-M15-1")

// Use to respect cross-compilation settings
addSbtPlugin("com.eed3si9n" % "sbt-doge" % "0.1.5")

