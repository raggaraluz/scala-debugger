package com.senkbeil.test.misc

/**
 * Provides an example of a main entrypoint using Scala's App instead of a
 * main method.
 *
 * @note Should have a class name of com.senkbeil.test.misc.MainUsingMethod
 * @note Should have breakpoint lines on 10 and 13
 */
object MainUsingMethod {
  def main(args: Array[String]) = {
    // Print out our arguments
    args.foreach(println)
  }
}
