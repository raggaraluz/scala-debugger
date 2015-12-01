package org.senkbeil.debugger.test.watchpoints

/**
 * Used to verify that access to fields can be observed via an
 * AccessWatchpoint request.
 */
object AccessWatchpoint extends App {
  val someAccessClass = new SomeAccessClass

  while (true) {
    println(s"SomeClass.field is ${someAccessClass.field}")

    Thread.sleep(100)
  }
}

class SomeAccessClass {
  val field = 999
}
