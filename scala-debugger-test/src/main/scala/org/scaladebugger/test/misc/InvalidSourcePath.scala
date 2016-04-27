package org.scaladebugger.test.invalid

/**
 * Represents a scenario where the package and class names do not match the
 * source path.
 */
object InvalidSourcePath {
  def main(args: Array[String]): Unit = {
    val c = new InvalidSourcePathClass

    val x = 1 + 1
    val y = c.getClass.getName

    x + y
  }
}

class InvalidSourcePathClass
