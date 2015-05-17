package com.senkbeil.test.steps

/**
 * Provides test of performing basic step in/out/over in Scala situations.
 *
 * @note Should have a class name of com.senkbeil.test.steps.BasicIterations
 */
object BasicIterations {
  def main(args: Array[String]) = {
    (new InnerClassNoArgs).runMeNoArgs()
    new InnerClassArgs(3, "test").runMeArgs(4, "another test")

    val totalIterations = 3

    // Test for comprehension
    for (x <- 1 to totalIterations) { noop(x) }

    // Test foreach
    (1 to totalIterations).foreach(noop(_))

    // Test map
    (1 to totalIterations).map(noop(_))

    // Test "reduce"
    (1 to totalIterations).foldLeft(0)((acc, i) => ret(i))

    // Create a function object that loops
    val myFunction = (x: Int) => (1 to x).foreach {
      noop(_)
    }

    // Create a method that loops
    def myMethod(x: Int) = (1 to x).foreach {
      noop(_)
    }

    myFunction(totalIterations)

    noop(None)
  }

  def noop(a: Any) = {}
  def ret[T](a: T): T = a

  class InnerClassNoArgs {
    def runMeNoArgs(): Unit = {}
  }

  class InnerClassArgs(var x: Int, var y: String) {
    def runMeArgs(newX: Int, newY: String) = {
      x = newX
      y = newY
    }
  }
}
