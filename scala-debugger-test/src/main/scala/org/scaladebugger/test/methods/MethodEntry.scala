package org.scaladebugger.test.methods

/**
 * Used to verify the MethodEntry manager.
 */
object MethodEntry extends App {
  def objectMethod() = {
    def innerMethod() = {
      val x = 1
      val y = 2
      x + y
    }

    innerMethod()
  }

  val a = objectMethod()
  val b = new MethodEntryTestClass().someOtherMethod()
  val c = new MethodEntryTestClass().testMethod()

  while (true) { Thread.sleep(1000) }
}

class MethodEntryTestClass {
  def testMethod() = {
    val x = 1
    val y = 2
    x + y
  }
  def someOtherMethod() = {}
}
