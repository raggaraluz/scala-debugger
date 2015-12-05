package org.senkbeil.debugger.api.lowlevel.vm

import java.util.concurrent.atomic.AtomicBoolean

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{TestUtilities, VirtualMachineFixtures}
import org.senkbeil.debugger.api.lowlevel.events.EventType._

class StandardVMDeathManagerIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(test.Constants.EventuallyTimeout),
    interval = scaled(test.Constants.EventuallyInterval)
  )

  describe("StandardVMDeathManager") {
    it("should trigger when a virtual machine dies") {
      val testClass = "org.senkbeil.debugger.test.misc.MainUsingApp"

      val detectedDeath = new AtomicBoolean(false)

      // Start our VM and listen for the start event
      withVirtualMachine(testClass) { (s) =>
        import s.lowlevel._

        // Mark that we want to receive vm death events and watch for one
        vmDeathManager.createVMDeathRequest()
        eventManager.addResumingEventHandler(VMDeathEventType, _ => {
          detectedDeath.set(true)
        })

        // Kill the JVM process so we get a disconnect event
        s.underlyingVirtualMachine.process().destroy()

        // Eventually, we should receive the death event
        logTimeTaken(eventually {
          detectedDeath.get() should be (true)
        })
      }
    }
  }
}
