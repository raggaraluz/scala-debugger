package org.senkbeil.debugger.api.lowlevel.watchpoints

import java.util.concurrent.atomic.AtomicBoolean

import com.sun.jdi.event.{ClassPrepareEvent, BreakpointEvent, AccessWatchpointEvent}
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.senkbeil.debugger.api.lowlevel.events.EventType._
import org.senkbeil.debugger.api.lowlevel.events.filters.MethodNameFilter
import test.{TestUtilities, VirtualMachineFixtures}

class AccessWatchpointManagerIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(test.Constants.EventuallyTimeout),
    interval = scaled(test.Constants.EventuallyInterval)
  )

  describe("AccessWatchpointManager") {
    it("should be able to detect access to a field") {
      val testClass = "org.senkbeil.debugger.test.watchpoints.AccessWatchpoint"
      val testFile = scalaClassStringToFileString(testClass)

      val className = "org.senkbeil.debugger.test.watchpoints.SomeAccessClass"
      val fieldName = "field"

      val detectedAccessWatchpoint = new AtomicBoolean(false)

      withVirtualMachine(testClass, suspend = false) { (v, s) =>
        import s.lowlevel._

        // TODO: This is currently in place to force our request to eventually
        //       be created - once we have pending requests, we should not do
        //       creation this way
        eventManager.addResumingEventHandler(ClassPrepareEventType, e => {
          val classPrepareEvent = e.asInstanceOf[ClassPrepareEvent]
          val name = classPrepareEvent.referenceType().name()

          // Once class with field is ready, create access watchpoint request
          if (name == className) {
            accessWatchpointManager.createAccessWatchpointRequest(
              className,
              fieldName
            )
          }
        })

        // Listen for access watchpoint events for specific variable
        eventManager.addResumingEventHandler(AccessWatchpointEventType, e => {
          val accessWatchpointEvent = e.asInstanceOf[AccessWatchpointEvent]
          val name = accessWatchpointEvent.field().name()

          // If we detected access for our variable, mark our flag
          if (name == fieldName) detectedAccessWatchpoint.set(true)
        })


        logTimeTaken(eventually {
          assert(detectedAccessWatchpoint.get(), s"$fieldName never accessed!")
        })
      }
    }
  }
}
