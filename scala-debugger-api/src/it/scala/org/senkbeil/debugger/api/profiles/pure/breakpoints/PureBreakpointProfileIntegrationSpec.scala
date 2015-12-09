package org.senkbeil.debugger.api.profiles.pure.breakpoints

import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}

import com.sun.jdi.event.BreakpointEvent
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.senkbeil.debugger.api.lowlevel.events.EventType
import org.senkbeil.debugger.api.lowlevel.events.EventType._
import org.senkbeil.debugger.api.profiles.pure.PureDebugProfile
import test.{TestUtilities, VirtualMachineFixtures}

class PureBreakpointProfileIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(test.Constants.EventuallyTimeout),
    interval = scaled(test.Constants.EventuallyInterval)
  )

  describe("PureBreakpointProfile") {
    it("should be able to set breakpoints within while loops") {
      val testClass = "org.senkbeil.debugger.test.breakpoints.WhileLoop"
      val testFile = scalaClassStringToFileString(testClass)

      val firstBreakpointLine = 13
      val firstBreakpointCount = new AtomicInteger(0)

      val secondBreakpointLine = 17
      val secondBreakpointCount = new AtomicInteger(0)

      withVirtualMachine(testClass) { (s) =>
        s.withProfile(PureDebugProfile.Name)
          .onBreakpoint(testFile, firstBreakpointLine)
          .get
          .map(_.location())
          .map(l => (l.sourcePath(), l.lineNumber()))
          .filter(_._1 == testFile)
          .filter(_._2 == firstBreakpointLine)
          .foreach(_ => firstBreakpointCount.incrementAndGet())

        s.withProfile(PureDebugProfile.Name)
          .onBreakpoint(testFile, secondBreakpointLine)
          .get
          .map(_.location())
          .map(l => (l.sourcePath(), l.lineNumber()))
          .filter(_._1 == testFile)
          .filter(_._2 == secondBreakpointLine)
          .foreach(_ => secondBreakpointCount.incrementAndGet())

        logTimeTaken(eventually {
          firstBreakpointCount.get() should be(10)
          secondBreakpointCount.get() should be(10)
        })
      }
    }

    it("should be able to set breakpoints within for comprehensions") {
      val testClass = "org.senkbeil.debugger.test.breakpoints.ForComprehension"
      val testFile = scalaClassStringToFileString(testClass)

      val firstBreakpointLine = 14
      val firstBreakpointCount = new AtomicInteger(0)

      val secondBreakpointLine = 18
      val secondBreakpointCount = new AtomicInteger(0)

      withVirtualMachine(testClass) { (s) =>
        s.withProfile(PureDebugProfile.Name)
          .onBreakpoint(testFile, firstBreakpointLine)
          .get
          .map(_.location())
          .map(l => (l.sourcePath(), l.lineNumber()))
          .filter(_._1 == testFile)
          .filter(_._2 == firstBreakpointLine)
          .foreach(_ => firstBreakpointCount.incrementAndGet())

        s.withProfile(PureDebugProfile.Name)
          .onBreakpoint(testFile, secondBreakpointLine)
          .get
          .map(_.location())
          .map(l => (l.sourcePath(), l.lineNumber()))
          .filter(_._1 == testFile)
          .filter(_._2 == secondBreakpointLine)
          .foreach(_ => secondBreakpointCount.incrementAndGet())

        logTimeTaken(eventually {
          firstBreakpointCount.get() should be(10)
          secondBreakpointCount.get() should be(10)
        })
      }
    }

    it("should be able to set breakpoints in a DelayInit object") {
      val testClass = "org.senkbeil.debugger.test.breakpoints.DelayedInit"
      val testFile = scalaClassStringToFileString(testClass)

      val firstBreakpointLine = 10
      val firstBreakpoint = new AtomicBoolean(false)

      val secondBreakpointLine = 11
      val secondBreakpoint = new AtomicBoolean(false)

      withVirtualMachine(testClass) { (s) =>
        s.withProfile(PureDebugProfile.Name)
          .onUnsafeBreakpoint(testFile, firstBreakpointLine)
          .map(_.location())
          .map(l => (l.sourcePath(), l.lineNumber()))
          .filter(_._1 == testFile)
          .filter(_._2 == firstBreakpointLine)
          .foreach(_ => firstBreakpoint.set(true))

        s.withProfile(PureDebugProfile.Name)
          .onUnsafeBreakpoint(testFile, secondBreakpointLine)
          .map(_.location())
          .map(l => (l.sourcePath(), l.lineNumber()))
          .filter(_._1 == testFile)
          .filter(_._2 == secondBreakpointLine)
          .foreach(_ => secondBreakpoint.set(true))

        logTimeTaken(eventually {
          // NOTE: Using asserts to provide more helpful failure messages
          assert(firstBreakpoint.get(), "First breakpoint not reached!")
          assert(secondBreakpoint.get(), "Second breakpoint not reached!")
        })
      }
    }
  }
}
