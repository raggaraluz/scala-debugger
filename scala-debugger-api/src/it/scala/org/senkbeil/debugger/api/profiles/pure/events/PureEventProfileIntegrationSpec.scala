package org.senkbeil.debugger.api.profiles.pure.events

import java.util.concurrent.atomic.AtomicInteger
import com.sun.jdi.event.BreakpointEvent
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.senkbeil.debugger.api.profiles.pure.PureDebugProfile
import org.senkbeil.debugger.api.lowlevel.events.EventType.BreakpointEventType
import test.{TestUtilities, VirtualMachineFixtures}

class PureEventProfileIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(test.Constants.EventuallyTimeout),
    interval = scaled(test.Constants.EventuallyInterval)
  )

  describe("PureEventProfile") {
    it("should receive events for the specified event type") {
      val testClass = "org.senkbeil.debugger.test.events.LoopingEvent"
      val testFile = scalaClassStringToFileString(testClass)
      val lineNumber1 = 13
      val lineNumber2 = 16
      val lineNumber3 = 19

      val hitLines = collection.mutable.Set[Int]()
      val eventCount = new AtomicInteger(0)

      withVirtualMachine(testClass, suspend = false) { (v, s) =>
        // Set our breakpoints
        s.withProfile(PureDebugProfile.Name).onBreakpoint(testFile, lineNumber1)
        s.withProfile(PureDebugProfile.Name).onBreakpoint(testFile, lineNumber2)
        s.withProfile(PureDebugProfile.Name).onBreakpoint(testFile, lineNumber3)

        // Set a separate event pipeline to receive events
        s.withProfile(PureDebugProfile.Name)
          .onUnsafeEvent(BreakpointEventType)
          .map(_.asInstanceOf[BreakpointEvent])
          .map(_.location().lineNumber())
          .foreach(lineNumber => {
            hitLines += lineNumber
            eventCount.incrementAndGet()
          })

        logTimeTaken(eventually {
          hitLines should contain allOf (lineNumber1, lineNumber2, lineNumber3)
          eventCount.get() should be > 0
        })
      }
    }

    it("should stop receiving events upon being closed") {
      val testClass = "org.senkbeil.debugger.test.events.LoopingEvent"
      val testFile = scalaClassStringToFileString(testClass)
      val lineNumber1 = 13
      val lineNumber2 = 16
      val lineNumber3 = 19

      val hitLines = collection.mutable.Set[Int]()
      val eventCount = new AtomicInteger(0)

      withVirtualMachine(testClass, suspend = false) { (v, s) =>
        // Set our breakpoints
        s.withProfile(PureDebugProfile.Name).onBreakpoint(testFile, lineNumber1)
        s.withProfile(PureDebugProfile.Name).onBreakpoint(testFile, lineNumber2)
        s.withProfile(PureDebugProfile.Name).onBreakpoint(testFile, lineNumber3)

        // Set a separate event pipeline to receive events
        val eventPipeline = s
          .withProfile(PureDebugProfile.Name)
          .onUnsafeEvent(BreakpointEventType)

        // Receive events
        eventPipeline
          .map(_.asInstanceOf[BreakpointEvent])
          .map(_.location().lineNumber())
          .foreach(lineNumber => {
            hitLines += lineNumber
            eventCount.incrementAndGet()
          })

        logTimeTaken(eventually {
          hitLines should contain allOf(lineNumber1, lineNumber2, lineNumber3)
          eventCount.get() should be > 0
        })

        /* Should have four breakpoint handlers from onBreakpoint and event */ {
          val currentBreakpointHandlers =
            s.lowlevel.eventManager.getHandlersForEventType(BreakpointEventType)
          currentBreakpointHandlers should have length (4)
        }

        // Stop receiving events
        eventPipeline.close(now = true)
        eventCount.set(0)

        // Should now only have breakpoint handlers for the onBreakpoint
        // calls and not the raw event pipeline
        {
          val currentBreakpointHandlers =
            s.lowlevel.eventManager.getHandlersForEventType(BreakpointEventType)
          currentBreakpointHandlers should have length (3)
          eventCount.get() should be(0)
        }
      }
    }
  }
}
