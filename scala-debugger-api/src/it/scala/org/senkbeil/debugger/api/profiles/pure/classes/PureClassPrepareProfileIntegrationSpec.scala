package org.senkbeil.debugger.api.profiles.pure.classes

import java.util.concurrent.atomic.{AtomicInteger, AtomicBoolean}

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.senkbeil.debugger.api.profiles.pure.PureDebugProfile
import test.{TestUtilities, VirtualMachineFixtures}

class PureClassPrepareProfileIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(test.Constants.EventuallyTimeout),
    interval = scaled(test.Constants.EventuallyInterval)
  )

  describe("PureClassPrepareProfile") {
    it("should trigger when a class is loaded") {
      val testClass = "org.senkbeil.debugger.test.classes.ClassPrepare"

      val expectedClassName = "org.senkbeil.debugger.test.classes.CustomClass"
      val classPrepareHit = new AtomicInteger(0)

      withVirtualMachine(testClass) { (s) =>
        // Mark that we want to receive class prepare events and watch for one
        // NOTE: This is already set within the ScalaVirtualMachine class
        s.withProfile(PureDebugProfile.Name)
          .onUnsafeClassPrepare()
          .map(_.referenceType().name())
          .filter(_ == expectedClassName)
          .foreach(_ => classPrepareHit.incrementAndGet())

        // Eventually, we should receive the class prepare event
        logTimeTaken(eventually {
          classPrepareHit.get() should be (1)
        })
      }
    }
  }
}
