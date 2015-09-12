logLevel := Level.Warn
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "4.0.0")

// Used for signing our artifacts which is a prerequisite for publishing them to Sonatype. Use 'sbt publishSigned'
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

// Publishes this project's artifacts to Maven Central using "sbt sonatypeRelease"
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "0.5.1")

