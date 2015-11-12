package org.senkbeil.debugger.test.monitors

/**
 * Provides test of monitor wait used to verify reception of monitor wait
 * events.
 */
object MonitorWait extends App {
  // The object of which threads will wait
  val obj = new Object

  // This thread's purpose is to wait on the object
  val t1 = new Thread(new Runnable {
    override def run(): Unit = while (true) obj.synchronized {
      obj.wait(100)
      Thread.sleep(1)
    }
  })

  // Start our waiting and notifying threads
  t1.start()

  // Block so we don't finish this run before detecting events
  while (true) { Thread.sleep(1000) }
}
