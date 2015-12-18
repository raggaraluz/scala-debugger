package org.scaladebugger.test.events

/**
 * Provides test of hitting a breakpoint event in a loop, used to verify
 * events received AND that stopping the reception of an event results in
 * not receiving it on the client anymore.
 *
 * @note Should place breakpoints on lines 13, 16, and 19.
 */
object LoopingEvent extends App {
  var x = 0

  while (true) {
    val newValue = if (x != 0) 0 else 1

    x = newValue

    // Release resources and make possible to interrupt
    Thread.sleep(1)
  }
}
