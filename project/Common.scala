import sbt._
import Keys._


object Common {
  def settings = Seq(
    version := "1.0",

    //scalaVersion := "2.10.4",
    crossScalaVersions := Seq("2.10.5", "2.11.6"),

    scalacOptions ++= Seq(
      "-encoding", "UTF-8", "-target:jvm-1.6",
      "-deprecation", "-unchecked", "-feature",
      "-Xfatal-warnings"
    ) ++ (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, scalaMajor)) if scalaMajor == 10 => Seq("-Ywarn-all")
      case _ => Nil
    }),

    javacOptions ++= Seq(
      "-source", "1.6", "-target", "1.6", "-Xlint:all", "-Werror",
      "-Xlint:-options", "-Xlint:-path", "-Xlint:-processing"
    ),

    testOptions in Test += Tests.Argument("-oDF")
  )
}