package org.senkbeil.debugger.test.watchpoints

/**
 * Used to verify that modification to fields can be observed via a
 * ModificationWatchpoint request.
 */
object ModificationWatchpoint extends App {
  var x = 0

  while (true) {
    x += 1

    Thread.sleep(100)
  }
}
