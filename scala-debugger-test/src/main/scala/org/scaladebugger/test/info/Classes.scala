package org.scaladebugger.test.info

/**
 * Provides test of examining class information.
 *
 * @note Should have a class name of org.scaladebugger.test.info.Classes
 */
object Classes {
  def main(args: Array[String]): Unit = {
    // Ensure that all desired classes are loaded
    val c1 = new ExternalNormalClass
    val c2 = ExternalCaseClass(0, "")
    val c3 = ExternalObjectClass
    val c4 = ExternalCaseObjectClass
    val c5 = new InternalNormalClass
    val c6 = InternalCaseClass(0, "")
    val c7 = InternalObjectClass
    val c8 = InternalCaseObjectClass

    while(true) Thread.sleep(1000)
  }

  class InternalNormalClass

  case class InternalCaseClass(x: Int, y: String)

  object InternalObjectClass

  case object InternalCaseObjectClass
}

class ExternalNormalClass {
  def method1(): Unit = {}

  def method2: String = "some value"

  def method3(x: Int): Int = x
}

case class ExternalCaseClass(x: Int, y: String)

object ExternalObjectClass

case object ExternalCaseObjectClass
