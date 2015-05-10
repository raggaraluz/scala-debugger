package com.senkbeil.debugger.breakpoints

import java.util.concurrent.atomic.AtomicInteger

import com.sun.jdi.event.{ClassPrepareEvent, BreakpointEvent}
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{TestUtilities, VirtualMachineFixtures}
import com.senkbeil.debugger.events.EventType._

class BreakpointManagerIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Span(20000, Milliseconds)),
    interval = scaled(Span(5, Milliseconds))
  )

  describe("BreakpointManager") {
    ignore("should be able to set a breakpoint in a DelayInit object") {
      val testClass = "com.senkbeil.test.breakpoints.DelayedInit"
      val testFile = scalaClassStringToFileString(testClass)
      val breakpointCounter = new AtomicInteger(0)
      withVirtualMachine(testClass) { (v, s) =>
        /*s.eventManager.addEventHandler(ClassPrepareEventType, e => {
          println("NEW CLASS: " + e.asInstanceOf[ClassPrepareEvent].referenceType().name())
        })
        s.classManager.allFileNames.filter(_.contains("senkbeil")).foreach(println)
        println("FILE = " + testFile)
        println("TEST")
        s.breakpointManager.setLineBreakpoint(testFile, 10) should be (true)
        println("TEST2")
        s.breakpointManager.setLineBreakpoint(testFile, 11) should be (true)

        s.eventManager.addEventHandler(BreakpointEventType, e => {
          //e.asInstanceOf[BreakpointEvent].location()
          breakpointCounter.incrementAndGet()
        })*/

        println("TEST3")

        eventually {
          breakpointCounter.get() should be (2)
        }
      }
    }
  }
}
