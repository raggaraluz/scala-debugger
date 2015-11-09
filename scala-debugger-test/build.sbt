lazy val buildResources = taskKey[String]("Builds code in resources directory")

buildResources := {
  import sys.process._

  val baseDir = (resourceDirectory in Compile).value

  val buildCommand = Seq("sh", "build.sh")
  val runCommand = Process(buildCommand, cwd = Some(baseDir))

  println(s"Executing '${buildCommand.mkString(" ")}' in ${baseDir.getPath}")
  runCommand.!!
}

// Update compile stage to build resource directory first
(compile in Compile) <<= (compile in Compile) dependsOn buildResources
