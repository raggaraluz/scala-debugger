import sbt._
import Keys._


object Common {
  def settings = Seq(
    version := "1.0",

    scalaVersion := "2.10.4",

    scalacOptions ++= Seq(
      "-deprecation", "-unchecked", "-feature",
      "-Xfatal-warnings",
      "-Ywarn-all"
    )
  )
}