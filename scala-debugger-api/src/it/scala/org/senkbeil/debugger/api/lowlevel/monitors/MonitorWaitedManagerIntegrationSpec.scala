package org.senkbeil.debugger.api.lowlevel.monitors

import java.util.concurrent.atomic.AtomicBoolean

import com.sun.jdi.event.MonitorWaitedEvent
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.senkbeil.debugger.api.lowlevel.events.EventType._
import test.{TestUtilities, VirtualMachineFixtures}

class MonitorWaitedManagerIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Span(5, Seconds)),
    interval = scaled(Span(5, Milliseconds))
  )

  describe("MonitorWaitedManager") {
    it("should trigger when a thread has finished waiting on a monitor object") {
      val testClass = "org.senkbeil.debugger.test.monitors.MonitorWaited"

      val detectedWaited = new AtomicBoolean(false)

      withVirtualMachine(testClass, suspend = false) { (v, s) =>
        import s.lowlevel._

        // Mark that we want to receive monitor waited events and
        // watch for one
        monitorWaitedManager.createMonitorWaitedRequest()
        eventManager.addResumingEventHandler(MonitorWaitedEventType, e => {
          val monitorWaitedEvent =
            e.asInstanceOf[MonitorWaitedEvent]

          val threadName = monitorWaitedEvent.thread().name()
          val monitorTypeName =
            monitorWaitedEvent.monitor().referenceType().name()

          logger.debug(s"Detected finished waiting in thread $threadName for monitor of type $monitorTypeName")
          detectedWaited.set(true)
        })

        // Eventually, we should receive the monitor waited event
        logTimeTaken(eventually {
          // NOTE: Using asserts to provide more helpful failure messages
          assert(detectedWaited.get(), s"No monitor finished waiting was detected!")
        })
      }
    }
  }
}
