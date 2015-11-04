package org.senkbeil.debugger.api.profiles.pure.vm

import java.util.concurrent.atomic.AtomicBoolean

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.senkbeil.debugger.api.profiles.pure.PureDebugProfile
import org.senkbeil.debugger.api.virtualmachines.ScalaVirtualMachine
import test.{TestUtilities, VirtualMachineFixtures}

class PureVMStartProfileIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Span(5, Seconds)),
    interval = scaled(Span(5, Milliseconds))
  )

  describe("PureVMStartProfile") {
    it("should trigger when a virtual machine starts") {
      val testClass = "org.senkbeil.debugger.test.misc.MainUsingApp"

      val detectedStart = new AtomicBoolean(false)

      def preStart(scalaVirtualMachine: ScalaVirtualMachine) = {
        scalaVirtualMachine
          .withProfile(PureDebugProfile.Name)
          .onUnsafeVMStart()
          .foreach(_ => detectedStart.set(true))
      }

      // Start our VM and listen for the start event
      withVirtualMachine(
        testClass,
        suspend = false,
        preStart = preStart
      ) { (v, s) =>
        // Eventually, we should receive the start event
        logTimeTaken(eventually {
          detectedStart.get() should be (true)
        })
      }
    }
  }
}
