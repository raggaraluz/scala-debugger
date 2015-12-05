package org.senkbeil.debugger.api.lowlevel.threads

import java.util.concurrent.atomic.{AtomicInteger, AtomicBoolean}

import com.sun.jdi.event.ThreadDeathEvent
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.senkbeil.debugger.api.lowlevel.events.EventType._
import test.{TestUtilities, VirtualMachineFixtures}

class StandardThreadDeathManagerIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(test.Constants.EventuallyTimeout),
    interval = scaled(test.Constants.EventuallyInterval)
  )

  describe("StandardThreadDeathManager") {
    it("should trigger when a thread dies") {
      val testClass = "org.senkbeil.debugger.test.threads.ThreadDeath"
      val testFile = scalaClassStringToFileString(testClass)

      val threadDeathCount = new AtomicInteger(0)

      // Start our Thread and listen for the start event
      withVirtualMachine(testClass) { (s) =>
        import s.lowlevel._

        val requestCreated = new AtomicBoolean(false)

        // Set a breakpoint first so we can be ready
        breakpointManager.createBreakpointRequest(testFile, 10)
        eventManager.addResumingEventHandler(BreakpointEventType, _ => {
          while (!requestCreated.get()) { Thread.sleep(1) }
        })

        // Mark that we want to receive thread death events
        threadDeathManager.createThreadDeathRequest()
        eventManager.addResumingEventHandler(ThreadDeathEventType, e => {
          val threadEvent = e.asInstanceOf[ThreadDeathEvent]
          val threadName = threadEvent.thread().name()

          logger.debug(s"Detected death of thread named $threadName")
          threadDeathCount.incrementAndGet()
        })

        requestCreated.set(true)

        // Eventually, we should receive a total of 10 thread deaths
        logTimeTaken(eventually {
          threadDeathCount.get() should be (10)
        })
      }
    }
  }
}
