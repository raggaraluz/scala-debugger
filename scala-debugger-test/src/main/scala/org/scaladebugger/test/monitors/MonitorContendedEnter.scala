package org.senkbeil.debugger.test.monitors

/**
 * Provides test of monitor contended enter used to verify reception of monitor
 * contended enter events.
 */
object MonitorContendedEnter extends App {
  // The synchronized method that threads will invoke
  def syncMethod(): Unit = synchronized { Thread.sleep(100) }

  val t1 = new Thread(new Runnable {
    override def run(): Unit = while (true) {
      syncMethod()
      Thread.sleep(1)
    }
  })

  val t2 = new Thread(new Runnable {
    override def run(): Unit = while (true) {
      syncMethod()
      Thread.sleep(1)
    }
  })

  // Start our invoking threads
  t1.start()
  t2.start()

  // Block so we don't finish this run before detecting events
  while (true) { Thread.sleep(1000) }
}
