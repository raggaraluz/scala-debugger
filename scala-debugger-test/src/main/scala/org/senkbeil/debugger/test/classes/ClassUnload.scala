package org.senkbeil.debugger.test.classes

import java.net.URLClassLoader

/**
 * Provides test of class unloading used to verify reception of class unload
 * events.
 */
object ClassUnload extends App {
  /* Load our test class from the test jar, then force out of scope */ {
    val testJarUrl = this.getClass.getResource("/TestJar.jar")
    val urlClassLoader = new URLClassLoader(Array(testJarUrl), null)

    val className = "org.senkbeil.debugger.test.jar.CustomClass"
    val c = urlClassLoader.loadClass(className)

    println(s"Loading classes from ${testJarUrl.getPath}")
    println(s"CustomClass: $c")
  }

  // Suggest garbage collecting our out-of-scope classloader to unload the
  // associated class
  System.gc()

  while (true) { Thread.sleep(1000) }
}
