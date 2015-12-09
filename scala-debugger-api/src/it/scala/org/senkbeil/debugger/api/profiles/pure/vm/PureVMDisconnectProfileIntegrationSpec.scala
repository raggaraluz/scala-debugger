package org.senkbeil.debugger.api.profiles.pure.vm

import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.senkbeil.debugger.api.profiles.pure.PureDebugProfile
import test.{TestUtilities, VirtualMachineFixtures}

class PureVMDisconnectProfileIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(test.Constants.EventuallyTimeout),
    interval = scaled(test.Constants.EventuallyInterval)
  )

  describe("PureVMDisconnectProfile") {
    it("should trigger when a virtual machine disconnects") {
      val testClass = "org.senkbeil.debugger.test.misc.MainUsingApp"

      val detectedDisconnect = new AtomicBoolean(false)

      // Start our VM and listen for the disconnect event
      withVirtualMachine(testClass) { (s) =>
        s.withProfile(PureDebugProfile.Name)
          .onUnsafeVMDisconnect()
          .foreach(_ => detectedDisconnect.set(true))

        // Kill the JVM process so we get a disconnect event
        s.underlyingVirtualMachine.process().destroy()

        // Eventually, we should receive the disconnect event
        logTimeTaken(eventually {
          detectedDisconnect.get() should be (true)
        })
      }
    }
  }
}
