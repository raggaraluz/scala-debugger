package org.senkbeil.debugger.test.threads

/**
 * Used to receive thread death events from the JVM.
 *
 * @note Set a breakpoint on line 10 to prepare to listen to events before they
 *       occur.
 */
object ThreadDeath extends App {
  val x = 1 + 1 // Breakpoint here to listen before threads start

  for (i <- 1 to 10) {
    val t = new Thread()
    t.setName("test thread " + i)
    t.start()
    t.interrupt()
  }

  while (true) { Thread.sleep(1000) }
}
