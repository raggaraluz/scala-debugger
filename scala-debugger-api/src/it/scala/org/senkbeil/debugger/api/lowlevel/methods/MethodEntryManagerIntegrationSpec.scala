package org.senkbeil.debugger.api.lowlevel.methods

import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}

import com.sun.jdi.event.{BreakpointEvent, MethodEntryEvent}
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.senkbeil.debugger.api.lowlevel.events.EventType
import org.senkbeil.debugger.api.lowlevel.events.EventType._
import org.senkbeil.debugger.api.events.filters.MethodNameFilter
import test.{TestUtilities, VirtualMachineFixtures}

class MethodEntryManagerIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Span(5, Seconds)),
    interval = scaled(Span(5, Milliseconds))
  )

  describe("MethodEntryManager") {
    it("should be able to detect entering a specific method in a class") {
      val testClass = "org.senkbeil.debugger.test.methods.MethodEntry"
      val testFile = scalaClassStringToFileString(testClass)

      val expectedClassName =
        "org.senkbeil.debugger.test.methods.MethodEntryTestClass"
      val expectedMethodName = "testMethod"
      val methodNameFilter = MethodNameFilter(name = expectedMethodName)

      val reachedUnexpectedMethod = new AtomicBoolean(false)
      val reachedExpectedMethod = new AtomicBoolean(false)
      val reachedMethodBeforeFirstLine = new AtomicBoolean(false)

      withVirtualMachine(testClass, suspend = false) { (v, s) =>
        import s.lowlevel._

        // Set up the method entry event
        methodEntryManager.setMethodEntry(
          expectedClassName,
          expectedMethodName
        )

        // First line in test method
        breakpointManager.setLineBreakpoint(testFile, 26)

        // Listen for breakpoint on first line of method, checking if this
        // breakpoint is hit before or after the method entry event
        eventManager.addResumingEventHandler(BreakpointEventType, e => {
          val breakpointEvent = e.asInstanceOf[BreakpointEvent]
          val location = breakpointEvent.location()
          val fileName = location.sourcePath()
          val lineNumber = location.lineNumber()

          logger.debug(s"Reached breakpoint: $fileName:$lineNumber")

          val methodEntryHit = reachedExpectedMethod.get()
          reachedMethodBeforeFirstLine.set(methodEntryHit)
        })

        // Listen for method entry events for the specific method
        eventManager.addResumingEventHandler(MethodEntryEventType, e => {
          val methodEntryEvent = e.asInstanceOf[MethodEntryEvent]
          val method = methodEntryEvent.method()
          val className = method.declaringType().name()
          val methodName = method.name()

          logger.debug(s"Reached method: $className/$methodName")

          if (className == expectedClassName && methodName == expectedMethodName) {
            reachedExpectedMethod.set(true)
          } else {
            reachedUnexpectedMethod.set(true)
          }
        }, methodNameFilter)

        logTimeTaken(eventually {
          reachedUnexpectedMethod.get() should be (false)
          reachedExpectedMethod.get() should be (true)
          reachedMethodBeforeFirstLine.get() should be (true)
        })
      }
    }
  }
}
