name := "DebuggerServer"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.5", // MIT
  "org.slf4j" % "slf4j-log4j12" % "1.7.5", // MIT
  "log4j" % "log4j" % "1.2.17"
)

// JDK Dependency (just for sbt, must exist on classpath for execution, cannot
// be redistributed)
internalDependencyClasspath in Compile += { Attributed.blank(Build.JavaTools) }

internalDependencyClasspath in Runtime += { Attributed.blank(Build.JavaTools) }

internalDependencyClasspath in Test += { Attributed.blank(Build.JavaTools) }

packSettings

packMain := Map("scala-debugger" -> "com.ibm.spark.kernel.Main")

