package org.senkbeil.debugger.api.lowlevel.exceptions

import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}

import com.sun.jdi.event.{BreakpointEvent, ClassPrepareEvent, ExceptionEvent}
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.senkbeil.debugger.api.lowlevel.events.EventType._
import org.senkbeil.debugger.api.virtualmachines.DummyScalaVirtualMachine
import test.{TestUtilities, VirtualMachineFixtures}

class StandardExceptionManagerIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(test.Constants.EventuallyTimeout),
    interval = scaled(test.Constants.EventuallyInterval)
  )

  describe("StandardExceptionManager") {
    it("should be able to detect exceptions in try blocks") {
      val testClass =
        "org.senkbeil.debugger.test.exceptions.InsideTryBlockException"
      val testFile = scalaClassStringToFileString(testClass)

      val detectedException = new AtomicBoolean(false)
      val expectedExceptionName =
        "org.senkbeil.debugger.test.exceptions.CustomException"

      val s = DummyScalaVirtualMachine.newInstance()
      import s.lowlevel._

      // Mark the exception we want to watch (now that the class
      // is available)
      exceptionManager.createExceptionRequest(
        exceptionName = expectedExceptionName,
        notifyCaught = true,
        notifyUncaught = false
      )

      eventManager.addResumingEventHandler(ExceptionEventType, e => {
        val exceptionEvent = e.asInstanceOf[ExceptionEvent]
        val exceptionName = exceptionEvent.exception().referenceType().name()

        logger.debug(s"Detected exception: $exceptionName")
        if (exceptionName == expectedExceptionName)
          detectedException.set(true)
      })

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
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

      val s = DummyScalaVirtualMachine.newInstance()
      import s.lowlevel._

      // Mark the exception we want to watch (now that the class
      // is available)
      exceptionManager.createExceptionRequest(
        exceptionName = expectedExceptionName,
        notifyCaught = true,
        notifyUncaught = false
      )

      eventManager.addResumingEventHandler(ExceptionEventType, e => {
        val exceptionEvent = e.asInstanceOf[ExceptionEvent]
        val exceptionName = exceptionEvent.exception().referenceType().name()

        logger.debug(s"Detected exception: $exceptionName")
        if (exceptionName == expectedExceptionName)
          detectedException.set(true)
      })

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
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

      val s = DummyScalaVirtualMachine.newInstance()
      import s.lowlevel._

      // Mark the exception we want to watch (now that the class
      // is available)
      exceptionManager.createExceptionRequest(
        exceptionName = expectedExceptionName,
        notifyCaught = false,
        notifyUncaught = true
      )

      eventManager.addResumingEventHandler(ExceptionEventType, e => {
        val exceptionEvent = e.asInstanceOf[ExceptionEvent]
        val exceptionName = exceptionEvent.exception().referenceType().name()

        logger.debug(s"Detected exception: $exceptionName")
        if (exceptionName == expectedExceptionName)
          detectedException.set(true)
      })

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          detectedException.get() should be (true)
        })
      }
    }
  }
}
