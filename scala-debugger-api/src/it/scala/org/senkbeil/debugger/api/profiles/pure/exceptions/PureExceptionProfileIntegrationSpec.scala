package org.senkbeil.debugger.api.profiles.pure.exceptions

import java.util.concurrent.atomic.AtomicBoolean

import com.sun.jdi.event.{BreakpointEvent, ExceptionEvent}
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.senkbeil.debugger.api.lowlevel.events.EventType._
import test.{TestUtilities, VirtualMachineFixtures}

class PureExceptionProfileIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Span(5, Seconds)),
    interval = scaled(Span(5, Milliseconds))
  )

  describe("PureExceptionProfile") {
    it("should be able to detect exceptions in try blocks") {
      val testClass =
        "org.senkbeil.debugger.test.exceptions.InsideTryBlockException"
      val testFile = scalaClassStringToFileString(testClass)

      val detectedException = new AtomicBoolean(false)
      val expectedExceptionName =
        "org.senkbeil.debugger.test.exceptions.CustomException"

      withVirtualMachine(testClass, suspend = false) { (v, s) =>
        // Use a breakpoint prior to our exceptions to prepare without passing
        // the exceptions
        s.onUnsafeBreakpoint(testFile, 10).map(_.location()).foreach(l => {
          // When breakpoint triggered, assume the exception class
          // has been loaded
          val fileName = l.sourcePath()
          val lineNumber = l.lineNumber()

          logger.debug(s"Reached breakpoint: $fileName:$lineNumber")

          // Mark the exception we want to watch (now that the class
          // is available)
          s.onUnsafeException(
            exceptionName = expectedExceptionName,
            notifyCaught = true,
            notifyUncaught = false
          ).map(_.exception().referenceType().name())
            .filter(_ == expectedExceptionName)
            .foreach(_ => detectedException.set(true))
        })

        logTimeTaken(eventually {
          detectedException.get() should be (true)
        })
      }
    }

    it("should be able to detect exceptions in functional try calls") {
      val testClass =
        "org.senkbeil.debugger.test.exceptions.InsideFunctionalTryException"
      val testFile = scalaClassStringToFileString(testClass)

      val detectedException = new AtomicBoolean(false)
      val expectedExceptionName =
        "org.senkbeil.debugger.test.exceptions.CustomException"

      withVirtualMachine(testClass, suspend = false) { (v, s) =>
        // Use a breakpoint prior to our exceptions to prepare without passing
        // the exceptions
        s.onUnsafeBreakpoint(testFile, 10).map(_.location()).foreach(l => {
          // When breakpoint triggered, assume the exception class
          // has been loaded
          val fileName = l.sourcePath()
          val lineNumber = l.lineNumber()

          logger.debug(s"Reached breakpoint: $fileName:$lineNumber")

          // Mark the exception we want to watch (now that the class
          // is available)
          s.onUnsafeException(
            exceptionName = expectedExceptionName,
            notifyCaught = true,
            notifyUncaught = false
          ).map(_.exception().referenceType().name())
            .filter(_ == expectedExceptionName)
            .foreach(_ => detectedException.set(true))
        })

        logTimeTaken(eventually {
          detectedException.get() should be (true)
        })
      }
    }

    it("should be able to detect exceptions outside of try blocks") {
      val testClass =
        "org.senkbeil.debugger.test.exceptions.OutsideTryException"
      val testFile = scalaClassStringToFileString(testClass)

      val detectedException = new AtomicBoolean(false)
      val expectedExceptionName =
        "org.senkbeil.debugger.test.exceptions.CustomException"

      withVirtualMachine(testClass, suspend = false) { (v, s) =>
        // Use a breakpoint prior to our exceptions to prepare without passing
        // the exceptions
        s.onUnsafeBreakpoint(testFile, 10).map(_.location()).foreach(l => {
          // When breakpoint triggered, assume the exception class
          // has been loaded
          val fileName = l.sourcePath()
          val lineNumber = l.lineNumber()

          logger.debug(s"Reached breakpoint: $fileName:$lineNumber")

          // Mark the exception we want to watch (now that the class
          // is available)
          s.onUnsafeException(
            exceptionName = expectedExceptionName,
            notifyCaught = false,
            notifyUncaught = true
          ).map(_.exception().referenceType().name())
            .filter(_ == expectedExceptionName)
            .foreach(_ => detectedException.set(true))
        })

        logTimeTaken(eventually {
          detectedException.get() should be (true)
        })
      }
    }
  }
}
