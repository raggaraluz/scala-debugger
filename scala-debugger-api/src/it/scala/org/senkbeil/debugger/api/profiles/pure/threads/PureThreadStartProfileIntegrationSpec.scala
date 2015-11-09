package org.senkbeil.debugger.api.profiles.pure.threads

import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.senkbeil.debugger.api.profiles.pure.PureDebugProfile
import test.{TestUtilities, VirtualMachineFixtures}

class PureThreadStartProfileIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Span(5, Seconds)),
    interval = scaled(Span(5, Milliseconds))
  )

  describe("PureThreadStartProfile") {
    it("should trigger when a thread starts") {
      val testClass = "org.senkbeil.debugger.test.threads.ThreadStart"
      val testFile = scalaClassStringToFileString(testClass)

      val threadStartCount = new AtomicInteger(0)

      // Start our Thread and listen for the start event
      withVirtualMachine(testClass, suspend = false) { (v, s) =>
        val requestCreated = new AtomicBoolean(false)

        // Set a breakpoint first so we can be ready
        s.withProfile(PureDebugProfile.Name)
          .onUnsafeBreakpoint(testFile, 10)
          .foreach(_ => {
            while (!requestCreated.get()) { Thread.sleep(1) }
          })

        // Mark that we want to receive thread start events
        s.withProfile(PureDebugProfile.Name)
          .onUnsafeThreadStart()
          .map(_.thread().name())
          .filterNot(_ == "main")
          .foreach(_ => threadStartCount.incrementAndGet())

        requestCreated.set(true)

        // Eventually, we should receive a total of 10 thread starts
        logTimeTaken(eventually {
          threadStartCount.get() should be (10)
        })
      }
    }

    it("should cache request creation based on arguments") {
      val testClass = "org.senkbeil.debugger.test.threads.ThreadStart"
      val testFile = scalaClassStringToFileString(testClass)

      val threadStartCount = new AtomicInteger(0)

      withVirtualMachine(testClass, suspend = false) { (v, s) =>
        val requestCreated = new AtomicBoolean(false)

        // Set a breakpoint first so we can be ready
        s.withProfile(PureDebugProfile.Name)
          .onUnsafeBreakpoint(testFile, 10)
          .foreach(_ => {
            while (!requestCreated.get()) {
              Thread.sleep(1)
            }
          })

        // Mark that we want to receive thread start events
        s.withProfile(PureDebugProfile.Name)
          .onUnsafeThreadStart()
          .map(_.thread().name())
          .filterNot(_ == "main")
          .foreach(_ => threadStartCount.incrementAndGet())

        // Perform a second mark with same input
        s.withProfile(PureDebugProfile.Name)
          .onUnsafeThreadStart()
          .map(_.thread().name())
          .filterNot(_ == "main")
          .foreach(_ => threadStartCount.incrementAndGet())

        requestCreated.set(true)

        // Eventually, we should receive a total of 10 + 10 thread starts
        logTimeTaken(eventually {
          threadStartCount.get() should be (20)
        })
      }
    }
  }
}
