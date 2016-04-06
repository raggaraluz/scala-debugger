package org.scaladebugger.test.info

/**
 * Serves to load multiple classes with the same souce name but different
 * source paths.
 */
object MultiSource {
  def main(args: Array[String]) = {
    val c1 = new package1.ScalaSource
    val c2 = new package2.ScalaSource

    while (true) { Thread.sleep(1000) }
  }
}
