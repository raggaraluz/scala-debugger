package org.senkbeil.debugger.api.profiles.pure.watchpoints

import java.util.concurrent.atomic.AtomicBoolean

import com.sun.jdi.event.AccessWatchpointEvent
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.senkbeil.debugger.api.lowlevel.events.EventType._
import org.senkbeil.debugger.api.profiles.pure.PureDebugProfile
import test.{TestUtilities, VirtualMachineFixtures}

class PureAccessWatchpointProfileIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(test.Constants.EventuallyTimeout),
    interval = scaled(test.Constants.EventuallyInterval)
  )

  describe("PureAccessWatchpointProfile") {
    it("should be able to detect access to a field") {
      val testClass = "org.senkbeil.debugger.test.watchpoints.AccessWatchpoint"
      val testFile = scalaClassStringToFileString(testClass)

      val className = "org.senkbeil.debugger.test.watchpoints.SomeAccessClass"
      val fieldName = "field"

      val detectedAccessWatchpoint = new AtomicBoolean(false)

      withVirtualMachine(testClass, suspend = false) { (v, s) =>
        // NOTE: Waiting for class to be ready before setting access watchpoint
        // TODO: Remove wait for class to be ready once pending functionality added
        s.withProfile(PureDebugProfile.Name)
          .onUnsafeClassPrepare()
          .map(_.referenceType().name())
          .filter(_ == className)
          .foreach(_ => {
            // Listen for access watchpoint events for specific variable
            s.withProfile(PureDebugProfile.Name)
              .onUnsafeAccessWatchpoint(className, fieldName)
              .filter(_.field().declaringType().name() == className)
              .filter(_.field().name() == fieldName)
              .foreach(_ => detectedAccessWatchpoint.set(true))
          })

        logTimeTaken(eventually {
          assert(detectedAccessWatchpoint.get(), s"$fieldName never accessed!")
        })
      }
    }
  }
}
