package org.senkbeil.debugger.api.lowlevel.classes

import java.util.concurrent.atomic.AtomicBoolean

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.senkbeil.debugger.api.lowlevel.events.EventType._
import test.{TestUtilities, VirtualMachineFixtures}

class ClassUnloadManagerIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Span(5, Seconds)),
    interval = scaled(Span(5, Milliseconds))
  )

  describe("ClassUnloadManager") {
    // NOTE: It is not possible to trigger a class unload accurately due to the
    //       JVM's non-deterministic nature with garbage collection.
    ignore("should trigger when a class is unloaded") {
      val testClass = "org.senkbeil.debugger.test.classes.ClassUnload"

      val detectedUnload = new AtomicBoolean(false)

      withVirtualMachine(testClass, suspend = false) { (v, s) =>
        import s.lowlevel._

        // Mark that we want to receive class unload events and watch for one
        classUnloadManager.createClassUnloadRequest()
        eventManager.addResumingEventHandler(ClassUnloadEventType, _ => {
          detectedUnload.set(true)
        })

        // Eventually, we should receive the class unload event
        logTimeTaken(eventually {
          detectedUnload.get() should be (true)
        })
      }
    }
  }
}
