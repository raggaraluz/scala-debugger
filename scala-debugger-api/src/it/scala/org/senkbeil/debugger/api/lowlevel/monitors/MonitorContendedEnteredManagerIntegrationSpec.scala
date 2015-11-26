package org.senkbeil.debugger.api.lowlevel.monitors

import java.util.concurrent.atomic.AtomicBoolean

import com.sun.jdi.event.MonitorContendedEnteredEvent
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.senkbeil.debugger.api.lowlevel.events.EventType._
import test.{TestUtilities, VirtualMachineFixtures}

class MonitorContendedEnteredManagerIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(test.Constants.EventuallyTimeout),
    interval = scaled(test.Constants.EventuallyInterval)
  )

  describe("MonitorContendedEnteredManager") {
    it("should trigger when a thread enters a monitor after waiting for it to be released by another thread") {
      val testClass = "org.senkbeil.debugger.test.monitors.MonitorContendedEntered"

      val detectedEntered = new AtomicBoolean(false)

      withVirtualMachine(testClass, suspend = false) { (v, s) =>
        import s.lowlevel._

        // Mark that we want to receive monitor contended entered events and
        // watch for one
        monitorContendedEnteredManager.createMonitorContendedEnteredRequest()
        eventManager.addResumingEventHandler(MonitorContendedEnteredEventType, e => {
          val monitorContendedEnteredEvent =
            e.asInstanceOf[MonitorContendedEnteredEvent]

          val threadName = monitorContendedEnteredEvent.thread().name()
          val monitorTypeName =
            monitorContendedEnteredEvent.monitor().referenceType().name()

          logger.debug(s"Detected monitor entered in thread $threadName for monitor of type $monitorTypeName")
          detectedEntered.set(true)
        })

        // Eventually, we should receive the monitor contended entered event
        logTimeTaken(eventually {
          // NOTE: Using asserts to provide more helpful failure messages
          assert(detectedEntered.get(), s"No monitor entered was detected!")
        })
      }
    }
  }
}
