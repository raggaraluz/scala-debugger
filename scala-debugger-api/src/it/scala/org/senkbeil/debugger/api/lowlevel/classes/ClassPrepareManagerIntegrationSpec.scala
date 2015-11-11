package org.senkbeil.debugger.api.lowlevel.classes

import java.util.concurrent.atomic.AtomicBoolean

import com.sun.jdi.event.ClassPrepareEvent
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.senkbeil.debugger.api.lowlevel.events.EventType._
import test.{TestUtilities, VirtualMachineFixtures}

class ClassPrepareManagerIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Span(5, Seconds)),
    interval = scaled(Span(5, Milliseconds))
  )

  describe("ClassPrepareManager") {
    it("should trigger when a class is loaded") {
      val testClass = "org.senkbeil.debugger.test.classes.ClassPrepare"

      val expectedClassName = "org.senkbeil.debugger.test.classes.CustomClass"
      val detectedPrepare = new AtomicBoolean(false)

      withVirtualMachine(testClass, suspend = false) { (v, s) =>
        import s.lowlevel._

        // Mark that we want to receive class prepare events and watch for one
        classPrepareManager.createClassPrepareRequest()
        eventManager.addResumingEventHandler(ClassPrepareEventType, e => {
          val classPrepareEvent = e.asInstanceOf[ClassPrepareEvent]
          val className = classPrepareEvent.referenceType().name()

          logger.debug("New class loaded: " + className)
          if (className == expectedClassName) detectedPrepare.set(true)
        })

        // Eventually, we should receive the class prepare event
        logTimeTaken(eventually {
          // NOTE: Using asserts to provide more helpful failure messages
          assert(detectedPrepare.get(), s"$expectedClassName was not loaded!")
        })
      }
    }
  }
}
