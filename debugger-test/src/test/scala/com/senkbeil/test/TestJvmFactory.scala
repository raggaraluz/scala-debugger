package com.senkbeil.test

import sys.process._

/**
 * Represents a factory to create test JVM instances.
 */
object TestJvmFactory {

  /**
   * Constructs the argument needed to have the JVM attempt to attach to a
   * remote debugger.
   *
   * @param address The address to attach to
   * @param port The port to attach to
   *
   * @return The argument as a string
   */
  def attachingJvmArgument(address: String, port: Int): String = {
    (
      s"-agentlib:jdwp=transport=dt_socket" ::
      s"server=n" ::
      s"suspend=n" ::
      s"address=$address:$port" ::
      Nil
    ).mkString(",")
  }

  /**
   * Creates a new test JVM instance.
   *
   * @param klass The class instance to use as the main entrypoint of the new
   *              JVM
   * @param jvmOptions The options to provide to the JVM on startup
   *
   * @return The reference to the JVM instance
   */
  def create(klass: Class[_], jvmOptions: Seq[String]): TestJvm = {
    // Determine the path to our executing Java instance (assume on PATH)
    val javaProgram = "java" //"which java".!!

    // Determine path to the main class
    val mainClassPath =
      klass.getProtectionDomain.getCodeSource.getLocation.getPath

    // Create the process builder to execute our JVM
    val jvmProcessBuilder = Process(
      javaProgram,
      jvmOptions
        :+ createClasspathArgument(".", mainClassPath)
        :+ klass.getName
    )

    val jvmProcess = jvmProcessBuilder.run()

    new TestJvm(jvmProcess)
  }

  @inline private def createClasspathArgument(paths: String*) =
    "-cp " + paths.mkString(java.io.File.pathSeparator)
}
