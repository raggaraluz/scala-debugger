package org.scaladebugger.test.monitors

/**
 * Provides test of monitor waited used to verify reception of monitor waited
 * events.
 */
object MonitorWaited extends App {
  // The object of which threads will wait and be notified
  val obj = new Object

  // This thread's purpose is to wait on the object
  val t1 = new Thread(new Runnable {
    override def run(): Unit = while (true) obj.synchronized {
      obj.wait()
      Thread.sleep(1)
    }
  })

  // This thread's purpose is to notify all other waiting threads for the object
  val t2 = new Thread(new Runnable {
    override def run(): Unit = while (true) obj.synchronized {
      obj.notifyAll()
      Thread.sleep(1)
    }
  })

  // Start our waiting and notifying threads
  t1.start()
  t2.start()

  // Block so we don't finish this run before detecting events
  while (true) { Thread.sleep(1000) }
}
