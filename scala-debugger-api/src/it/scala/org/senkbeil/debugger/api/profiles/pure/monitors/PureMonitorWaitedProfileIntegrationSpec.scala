package org.senkbeil.debugger.api.profiles.pure.monitors

import java.util.concurrent.atomic.AtomicBoolean

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.senkbeil.debugger.api.profiles.pure.PureDebugProfile
import test.{TestUtilities, VirtualMachineFixtures}

class PureMonitorWaitedProfileIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(test.Constants.EventuallyTimeout),
    interval = scaled(test.Constants.EventuallyInterval)
  )

  describe("PureMonitorWaitedProfile") {
    it("should trigger when a thread has finished waiting on a monitor object") {
      val testClass = "org.senkbeil.debugger.test.monitors.MonitorWaited"

      val detectedWaited = new AtomicBoolean(false)

      withVirtualMachine(testClass, suspend = false) { (v, s) =>
        // Mark that we want to receive monitor waited events and
        // watch for one
        s.withProfile(PureDebugProfile.Name)
          .onUnsafeMonitorWaited()
          .foreach(_ => detectedWaited.set(true))

        // Eventually, we should receive the monitor waited event
        logTimeTaken(eventually {
          // NOTE: Using asserts to provide more helpful failure messages
          assert(detectedWaited.get(), s"No monitor finished waiting was detected!")
        })
      }
    }
  }
}
