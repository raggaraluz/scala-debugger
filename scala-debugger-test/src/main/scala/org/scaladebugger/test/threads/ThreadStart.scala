package org.scaladebugger.test.threads

/**
 * Used to receive thread start events from the JVM.
 *
 * @note Set a breakpoint on line 10 to prepare to listen to events before they
 *       occur.
 */
object ThreadStart extends App {
  val x = 1 + 1 // Breakpoint here to listen before threads start

  for (i <- 1 to 10) {
    val t = new Thread()
    t.setName("test thread " + i)
    t.start()
  }

  while (true) { Thread.sleep(1000) }
}
