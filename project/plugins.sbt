logLevel := Level.Warn

resolvers += Resolver.sonatypeRepo("snapshots")

resolvers += Resolver.sonatypeRepo("releases")

// Used for formatting source to certain style
addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.3.0")

// Used for style notes
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.6.0")

// Used for signing in order to publish jars
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

// Used to ensure proper publish process is followed
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "0.5.0")

