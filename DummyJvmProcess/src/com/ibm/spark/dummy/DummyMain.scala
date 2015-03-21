package com.ibm.spark.dummy

object DummyMain extends App {
  val x = 3
  var y = 4
  val x123 = "huh?"

  def runMe(x: Int = 3) = println(x)

  while (true) {
    val z = x + y
    println("Running " + runMe())
    Thread.sleep(1000)
    println("Past sleep!")
  }

  y = 5
  runMe()
}
