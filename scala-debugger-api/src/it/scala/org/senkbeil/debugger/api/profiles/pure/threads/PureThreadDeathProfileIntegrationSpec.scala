package org.senkbeil.debugger.api.profiles.pure.threads

import java.util.concurrent.atomic.{AtomicInteger, AtomicBoolean}

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.senkbeil.debugger.api.profiles.pure.PureDebugProfile
import test.{TestUtilities, VirtualMachineFixtures}

class PureThreadDeathProfileIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Span(5, Seconds)),
    interval = scaled(Span(5, Milliseconds))
  )

  describe("PureThreadDeathProfile") {
    it("should trigger when a thread dies") {
      val testClass = "org.senkbeil.debugger.test.threads.ThreadDeath"
      val testFile = scalaClassStringToFileString(testClass)

      val threadDeathCount = new AtomicInteger(0)

      withVirtualMachine(testClass, suspend = false) { (v, s) =>
        val requestCreated = new AtomicBoolean(false)

        // Set a breakpoint first so we can be ready
        s.withProfile(PureDebugProfile.Name)
          .onUnsafeBreakpoint(testFile, 10)
          .foreach(_ => {
            while (!requestCreated.get()) { Thread.sleep(1) }
          })

        // Mark that we want to receive thread death events
        s.withProfile(PureDebugProfile.Name)
          .onUnsafeThreadDeath()
          .foreach(_ => threadDeathCount.incrementAndGet())

        requestCreated.set(true)

        // Eventually, we should receive a total of 10 thread deaths
        logTimeTaken(eventually {
          threadDeathCount.get() should be (10)
        })
      }
    }
  }
}
