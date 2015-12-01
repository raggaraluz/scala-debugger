package org.senkbeil.debugger.test.watchpoints

/**
 * Used to verify that access to fields can be observed via an
 * AccessWatchpoint request.
 */
object AccessWatchpoint extends App {
  val someClass = new SomeClass

  while (true) {
    println(s"SomeClass.field is ${someClass.field}")

    Thread.sleep(100)
  }
}

class SomeClass {
  val field = 999
}
