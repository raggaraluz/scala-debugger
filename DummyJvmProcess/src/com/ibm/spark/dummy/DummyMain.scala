package com.ibm.spark.dummy

object DummyMain extends App {
  val x = 3
  var y = 4

  def runMe(x: Int = 3) = println(x)

  while (true) {
    println("Running " + runMe())
    Thread.sleep(1000)
  }
}

