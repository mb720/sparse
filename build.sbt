name := "Sparse"
// This defines where we publish this project's artifacts (via "sbt publishSigned")
organization := "eu.matthiasbraun"
name:= "sparse"
version := "1.0"
scalaVersion := "2.11.6"

// Disable using the Scala version in output paths and artifacts as a postfix
crossPaths := false

libraryDependencies += "com.jsuereth" %% "scala-arm" % "1.4"

publishMavenStyle := true

pgpSecretRing := file("local.secring.asc")
pgpPublicRing := file("local.pubring.asc")

licenses := Seq("GNU Lesser General Public License" -> url("https://www.gnu.org/licenses/lgpl-3.0.en.html"))
homepage := Some(url("https://github.com/mb720/sparse"))

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if(isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

pomExtra := (
<scm>
<url>git@github.com:mb720/sparse.git</url>
<connection>scm:git:git@github.com:mb720/sparse.git</connection>
</scm>
<developers>
<developer>
<id>mb720</id>
<name>Matthias Braun</name>
<url>http://www.bullbytes.com</url>
</developer>
</developers>)

