package org.senkbeil.debugger.api.profiles.pure.methods

import java.util.concurrent.atomic.{AtomicInteger, AtomicBoolean}

import com.sun.jdi.event.{BreakpointEvent, MethodExitEvent}
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.senkbeil.debugger.api.lowlevel.events.EventType._
import org.senkbeil.debugger.api.lowlevel.events.filters.MethodNameFilter
import org.senkbeil.debugger.api.profiles.pure.PureDebugProfile
import test.{TestUtilities, VirtualMachineFixtures}

class PureMethodExitProfileIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Span(5, Seconds)),
    interval = scaled(Span(5, Milliseconds))
  )

  describe("PureMethodExitProfile") {
    it("should be able to detect exiting a specific method in a class") {
      val testClass = "org.senkbeil.debugger.test.methods.MethodExit"
      val testFile = scalaClassStringToFileString(testClass)

      val expectedClassName =
        "org.senkbeil.debugger.test.methods.MethodExitTestClass"
      val expectedMethodName = "testMethod"

      val leftUnexpectedMethod = new AtomicBoolean(false)
      val leftExpectedMethod = new AtomicBoolean(false)
      val leftMethodAfterLastLine = new AtomicBoolean(false)

      withVirtualMachine(testClass, suspend = false) { (v, s) =>
        val methodPipeline = s.withProfile(PureDebugProfile.Name)
          .onUnsafeMethodExit(expectedClassName, expectedMethodName)
          .map(_.method())
          .map(m => (m.declaringType().name(), m.name()))

        methodPipeline
          .filter(_._1 == expectedClassName)
          .filter(_._2 == expectedMethodName)
          .foreach(_ => leftExpectedMethod.set(true))

        methodPipeline
          .filterNot(_._1 == expectedClassName)
          .foreach(_ => leftUnexpectedMethod.set(true))

        methodPipeline
          .filterNot(_._2 == expectedMethodName)
          .foreach(_ => leftUnexpectedMethod.set(true))

        // Last line in test method
        s.onUnsafeBreakpoint(testFile, 28)
          .map(_.location())
          .map(l => (l.sourcePath(), l.lineNumber()))
          .foreach(t => {
            val methodExitHit = leftExpectedMethod.get()
            leftMethodAfterLastLine.set(!methodExitHit)
          })

        logTimeTaken(eventually {
          leftUnexpectedMethod.get() should be (false)
          leftExpectedMethod.get() should be (true)
          leftMethodAfterLastLine.get() should be (true)
        })
      }
    }
  }
}
