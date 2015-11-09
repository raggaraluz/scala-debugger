package org.senkbeil.debugger.test.classes

import java.net.URLClassLoader

/**
 * Provides test of class preparation (loading) used to verify reception
 * of class prepare events.
 */
object ClassPrepare extends App {
  // Load our test class from the test jar
  val testJarUrl = this.getClass.getResource("/TestJar.jar")
  val urlClassLoader = new URLClassLoader(Array(testJarUrl), null)

  val className = "org.senkbeil.debugger.test.jar.CustomClass"
  val c = urlClassLoader.loadClass(className)

  println(s"Loading classes from ${testJarUrl.getPath}")
  println(s"CustomClass: $c")

  while (true) { Thread.sleep(1000) }
}
