package org.senkbeil.debugger.test.classes

/**
 * Provides test of class preparation (loading) used to verify reception
 * of class prepare events.
 */
object ClassPrepare extends App {
  // Load our class
  val customClass = new CustomClass

  while (true) { Thread.sleep(1000) }
}
