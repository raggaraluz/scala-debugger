package org.senkbeil.debugger.api.lowlevel.watchpoints

import java.util.concurrent.atomic.AtomicBoolean

import com.sun.jdi.event.{BreakpointEvent, AccessWatchpointEvent}
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

      val className = "org.senkbeil.debugger.test.watchpoints.SomeClass"
      val fieldName = "field"

      val detectedAccessWatchpoint = new AtomicBoolean(false)

      withVirtualMachine(testClass, suspend = false) { (v, s) =>
        import s.lowlevel._

        // TODO: This is currently in place to force our request to eventually
        //       be created - once we have pending requests, we should not do
        //       creation this way
        @volatile var request: Option[String] = None

        // Listen for access watchpoint events for specific variable
        eventManager.addResumingEventHandler(AccessWatchpointEventType, e => {
          val accessWatchpointEvent = e.asInstanceOf[AccessWatchpointEvent]
          val name = accessWatchpointEvent.field().name()

          // If we detected access for our variable, mark our flag
          if (name == fieldName) detectedAccessWatchpoint.set(true)
        })


        logTimeTaken(eventually {
          // Set up the access watchpoint event
          if (request.isEmpty) {
            request = accessWatchpointManager.createAccessWatchpointRequestByName(
              className,
              fieldName
            ).toOption
          }

          assert(detectedAccessWatchpoint.get(), s"$fieldName never accessed!")
        })
      }
    }
  }
}
