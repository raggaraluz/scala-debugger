package org.senkbeil.debugger.api.profiles.pure.vm

import java.util.concurrent.atomic.{AtomicInteger, AtomicBoolean}

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.senkbeil.debugger.api.lowlevel.events.EventType._
import org.senkbeil.debugger.api.profiles.pure.PureDebugProfile
import test.{TestUtilities, VirtualMachineFixtures}

class PureVMDeathProfileIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Span(5, Seconds)),
    interval = scaled(Span(5, Milliseconds))
  )

  describe("PureVMDeathProfile") {
    it("should trigger when a virtual machine dies") {
      val testClass = "org.senkbeil.debugger.test.misc.MainUsingApp"

      val detectedDeath = new AtomicBoolean(false)

      // Start our VM and listen for the start event
      withVirtualMachine(testClass, suspend = false) { (v, s) =>
        // Mark that we want to receive vm death events and watch for one
        s.withProfile(PureDebugProfile.Name)
          .onUnsafeVMDeath()
          .foreach(_ => detectedDeath.set(true))

        // Kill the JVM process so we get a disconnect event
        s.underlyingVirtualMachine.process().destroy()

        // Eventually, we should receive the start event
        logTimeTaken(eventually {
          detectedDeath.get() should be (true)
        })
      }
    }

    it("should cache request creation based on arguments") {
      val testClass = "org.senkbeil.debugger.test.misc.MainUsingApp"

      val detectedDeathHit = new AtomicInteger(0)

      // Start our VM and listen for the start event
      withVirtualMachine(testClass, suspend = false) { (v, s) =>
        // Mark that we want to receive vm death events and watch for one
        s.withProfile(PureDebugProfile.Name)
          .onUnsafeVMDeath()
          .foreach(_ => detectedDeathHit.incrementAndGet())

        // Perform the same check with same arguments
        s.withProfile(PureDebugProfile.Name)
          .onUnsafeVMDeath()
          .foreach(_ => detectedDeathHit.incrementAndGet())

        // Kill the JVM process so we get a disconnect event
        s.underlyingVirtualMachine.process().destroy()

        // Eventually, we should receive the start event
        logTimeTaken(eventually {
          detectedDeathHit.get() should be (2)
        })
      }
    }
  }
}
