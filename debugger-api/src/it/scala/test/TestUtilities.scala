package test

import com.senkbeil.utils.LogLike

/**
 * Contains helper methods for testing.
 */
trait TestUtilities { this: LogLike =>
  /**
   * Converts a class string to a file string.
   *
   * @example com.senkbeil.MyClass becomes com/senkbeil/MyClass.scala
   *
   * @param classString The class string to convert
   *
   * @return The resulting file string
   */
  def scalaClassStringToFileString(classString: String) =
    classString.replace('.', java.io.File.separatorChar) + ".scala"

  /**
   * Executes the block of code and logs the time taken to evaluate it.
   *
   * @param block The block of code to execute
   * @tparam T The return type of the block of code
   *
   * @return The value returned from the block of code
   */
  def logTimeTaken[T](block: => T): T = {
    val startTime = System.currentTimeMillis()

    try {
      block
    } finally {
      val finalTime = System.currentTimeMillis() - startTime
      logger.info(s"Time taken: ${finalTime / 1000.0}s")
    }
  }
}
