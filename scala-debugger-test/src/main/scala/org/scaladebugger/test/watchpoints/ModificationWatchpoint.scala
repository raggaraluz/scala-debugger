package org.senkbeil.debugger.test.watchpoints

/**
 * Used to verify that modification to fields can be observed via a
 * ModificationWatchpoint request.
 */
object ModificationWatchpoint extends App {
  val someModificationClass = new SomeModificationClass

  while (true) {
    someModificationClass.field += 1

    Thread.sleep(100)
  }
}

class SomeModificationClass {
  var field = 0
}
