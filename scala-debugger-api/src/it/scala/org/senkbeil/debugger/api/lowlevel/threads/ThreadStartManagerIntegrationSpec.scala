package org.senkbeil.debugger.api.lowlevel.threads

import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}

import com.sun.jdi.event.ThreadStartEvent
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.senkbeil.debugger.api.lowlevel.events.EventType._
import test.{TestUtilities, VirtualMachineFixtures}

class ThreadStartManagerIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Span(5, Seconds)),
    interval = scaled(Span(5, Milliseconds))
  )

  describe("ThreadStartManager") {
    it("should trigger when a thread starts") {
      val testClass = "org.senkbeil.debugger.test.threads.ThreadStart"
      val testFile = scalaClassStringToFileString(testClass)

      val threadStartCount = new AtomicInteger(0)

      // Start our Thread and listen for the start event
      withVirtualMachine(testClass, suspend = false) { (v, s) =>
        import s.lowlevel._

        val requestCreated = new AtomicBoolean(false)

        // Set a breakpoint first so we can be ready
        breakpointManager.createLineBreakpointRequest(testFile, 10)
        eventManager.addResumingEventHandler(BreakpointEventType, _ => {
          while (!requestCreated.get()) { Thread.sleep(1) }
        })

        // Mark that we want to receive thread start events
        threadStartManager.createThreadStartRequest()
        eventManager.addResumingEventHandler(ThreadStartEventType, e => {
          val threadEvent = e.asInstanceOf[ThreadStartEvent]
          val threadName = threadEvent.thread().name()

          logger.debug(s"Detected start of thread named $threadName")
          if (threadName != "main") threadStartCount.incrementAndGet()
        })

        requestCreated.set(true)

        // Eventually, we should receive a total of 10 thread starts
        logTimeTaken(eventually {
          threadStartCount.get() should be (10)
        })
      }
    }
  }
}
