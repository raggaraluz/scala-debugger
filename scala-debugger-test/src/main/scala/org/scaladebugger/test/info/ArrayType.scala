package org.scaladebugger.test.info

/**
 * Provides tests for examining array type information.
 *
 * @note Should have a class name of org.scaladebugger.test.info.ArrayType
 */
object ArrayType {
  def main(args: Array[String]): Unit = {
    val array: Array[Int] = Array(1, 2, 3)
    var tmpArray: Array[Int] = Array()

    val newArray: Array[Int] = Array(tmpArray.length)

    val total = array ++ newArray
  }
}
