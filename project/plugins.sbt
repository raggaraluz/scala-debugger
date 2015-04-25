logLevel := Level.Warn

resolvers += Resolver.sonatypeRepo("snapshots")

resolvers += Resolver.sonatypeRepo("releases")

// Used for packaging an application
addSbtPlugin("org.xerial.sbt" % "sbt-pack" % "0.6.8")

// Used for formatting source to certain style
addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.3.0")

// Used for style notes
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.6.0")