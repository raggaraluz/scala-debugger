package com.senkbeil.debugger.steps

import java.util.concurrent.atomic.AtomicBoolean

import com.sun.jdi.event.{StepEvent, BreakpointEvent}
import org.scalatest.concurrent.Eventually
import org.scalatest.concurrent.PatienceConfiguration.{Timeout, Interval}
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
    describe("stepping over") {
      it("should be able to step back out to higher frame once method finishes") {
        val testClass = "com.senkbeil.test.steps.BasicIterations"
        val testFile = scalaClassStringToFileString(testClass)

        // Start on last line of a method
        val startingLine = 52

        // Should return to higher frame on next line
        val expectedLine = 13

        // Flag that indicates we reached the expected line
        val success = new AtomicBoolean(false)

        withVirtualMachine(testClass, suspend = false) { (v, s) =>
          s.breakpointManager.setLineBreakpoint(testFile, startingLine)

          // On receiving a breakpoint, send a step request
          s.eventManager.addResumingEventHandler(BreakpointEventType, e => {
            val breakpointEvent = e.asInstanceOf[BreakpointEvent]
            val className = breakpointEvent.location().declaringType().name()
            val lineNumber = breakpointEvent.location.lineNumber()

            logger.debug(s"Hit breakpoint at $className:$lineNumber")
            s.stepManager.stepOver(breakpointEvent.thread())
          })

          // On receiving a step request, verify that we are in the right
          // location
          s.eventManager.addResumingEventHandler(StepEventType, e => {
            val stepEvent = e.asInstanceOf[StepEvent]
            val className = stepEvent.location().declaringType().name()
            val lineNumber = stepEvent.location().lineNumber()

            logger.debug(s"Stepped onto $className:$lineNumber")
            success.set(lineNumber == expectedLine)
          })

          logTimeTaken(eventually {
            // NOTE: Using asserts to provide more helpful failure messages
            assert(success.get(), s"Did not reach $testClass:$expectedLine!")
          })
        }
      }

      it("should be able to step over all lines in a method") {
        val testClass = "com.senkbeil.test.steps.BasicIterations"
        val testFile = scalaClassStringToFileString(testClass)

        // Start on first line of main method
        val startingLine = 10

        val expectedReachableLines = Seq(11, 13, 16, 19, 22, 25, 28, 37, 39)
        val expectedLines = collection.mutable.Map[Int, Boolean](
          expectedReachableLines.map((_, false)): _*
        )

        withVirtualMachine(testClass, suspend = false) { (v, s) =>
          // Add a breakpoint to get us in the right location for steps
          s.breakpointManager.setLineBreakpoint(testFile, startingLine)

          // On receiving a breakpoint, send a step request
          s.eventManager.addResumingEventHandler(BreakpointEventType, e => {
            val breakpointEvent = e.asInstanceOf[BreakpointEvent]
            val className = breakpointEvent.location().declaringType().name()
            val lineNumber = breakpointEvent.location.lineNumber()

            logger.debug(s"Hit breakpoint at $className:$lineNumber")
            s.stepManager.stepOver(breakpointEvent.thread())
          })

          // On receiving a step request, verify that we are in the right
          // location
          s.eventManager.addResumingEventHandler(StepEventType, e => {
            val stepEvent = e.asInstanceOf[StepEvent]
            val className = stepEvent.location().declaringType().name()
            val lineNumber = stepEvent.location().lineNumber()

            logger.debug(s"Stepped onto $className:$lineNumber")

            // Mark the line as stepped on if valid
            if (className.contains(testClass)) expectedLines(lineNumber) = true

            // Continue stepping if not reached all lines
            if (!expectedLines.values.reduce(_ && _))
              s.stepManager.stepOver(stepEvent.thread())
          })

          // Time taken appears to be hovering around 5 seconds, so upping the
          // maximum timeout (long time due to for comprehension)
          logTimeTaken(eventually(
            timeout = Timeout(scaled(Span(7, Seconds))),
            interval = Interval(scaled(Span(5, Milliseconds)))
          ) {
            // NOTE: Using asserts to provide more helpful failure messages
            expectedLines.foreach { case (lineNumber, wasReached) =>
              assert(wasReached, s"Line $lineNumber was not reached!")
            }
          })
        }
      }
    }

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
            val className = breakpointEvent.location().declaringType().name()
            val lineNumber = breakpointEvent.location.lineNumber()

            logger.debug(s"Hit breakpoint at $className:$lineNumber")
            s.stepManager.stepInto(breakpointEvent.thread())
          })

          // On receiving a step request, verify that we are in the right
          // location
          s.eventManager.addResumingEventHandler(StepEventType, e => {
            val stepEvent = e.asInstanceOf[StepEvent]
            val className = stepEvent.location().declaringType().name()
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
