package com.senkbeil.debugger.steps

import java.util.concurrent.atomic.AtomicBoolean

import com.sun.jdi.event.{StepEvent, BreakpointEvent}
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{TestUtilities, VirtualMachineFixtures}
import com.senkbeil.debugger.events.EventType._

class StepManagerIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Span(5, Seconds)),
    interval = scaled(Span(5, Milliseconds))
  )

  describe("StepManager") {
    describe("stepping into") {
      it("should be able to step into a zero-argument function in a class") {
        val testClass = "com.senkbeil.test.steps.BasicIterations"
        val testFile = scalaClassStringToFileString(testClass)

        val startingLine = 10
        val expectedLine = 46
        val success = new AtomicBoolean(false)

        withVirtualMachine(testClass, suspend = false) { (v, s) =>
          // Add a breakpoint to get us in the right location for steps
          s.breakpointManager.setLineBreakpoint(testFile, startingLine)

          // On receiving a breakpoint, send a step request
          s.eventManager.addResumingEventHandler(BreakpointEventType, e => {
            val breakpointEvent = e.asInstanceOf[BreakpointEvent]
            val lineNumber = breakpointEvent.location.lineNumber()
            val className = breakpointEvent.thread().name()

            logger.debug(s"Hit breakpoint at $className:$lineNumber")
            s.stepManager.stepInto(breakpointEvent.thread())
          })

          // On receiving a step request, verify that we are in the right
          // location
          s.eventManager.addResumingEventHandler(StepEventType, e => {
            val stepEvent = e.asInstanceOf[StepEvent]
            val className = stepEvent.thread().name()
            val lineNumber = stepEvent.location().lineNumber()

            logger.debug(s"Stepped onto $className:$lineNumber")
            success.set(lineNumber == expectedLine)
            s.stepManager.stepInto(stepEvent.thread())
          })

          logTimeTaken(eventually {
            // NOTE: Using asserts to provide more helpful failure messages
            assert(success.get(), s"Did not reach $testClass:$expectedLine!")
          })
        }
      }
    }

  }
}
