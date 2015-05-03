package com.senkbeil.debugger.breakpoints

import com.senkbeil.debugger.classes.ClassManager
import com.sun.jdi.VirtualMachine
import com.sun.jdi.request.EventRequestManager
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}

class BreakpointManagerSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{
  private val mockEventRequestManager = mock[EventRequestManager]
  private val stubVirtualMachine = stub[VirtualMachine]
  (stubVirtualMachine.eventRequestManager _).when()
    .returns(mockEventRequestManager)

  // NOTE: Needed until https://github.com/paulbutcher/ScalaMock/issues/56
  class ZeroArgClassManager
    extends ClassManager(stubVirtualMachine, loadClasses = false)
  private val mockClassManager = mock[ZeroArgClassManager]

  private val breakpointManager = new BreakpointManager(
    stubVirtualMachine,
    mockClassManager
  )

  describe("BreakpointManager") {
    describe("#breakpointList") {
      it("should return a collection of breakpoint file names and lines") {
        val expected = Seq(("file", 1), ("file", 2))

        expected.foreach(b => breakpointManager.setLineBreakpoint(b._1, b._2))

        val actual = breakpointManager.breakpointList

        actual should contain theSameElementsAs expected
      }

      it("should return an empty collection if no breakpoints have been set") {
        breakpointManager.breakpointList should be (empty)
      }
    }

    describe("#setLineBreakpoint") {
      it("should throw an exception if the file is not available") {
        fail()
      }

      it("should throw an exception if the line is not available") {
        fail()
      }

      it("should return true if successfully added the breakpoint") {
        fail()
      }

      it("should create a new breakpoint request for each matching location") {
        fail()
      }

      it("should return false if unable to create one of the underlying breakpoint requests") {
        fail()
      }
    }

    describe("#hasLineBreakpoint") {
      it("should return true if the breakpoint with matching file name and line is found") {
        fail()
      }

      it("should return false if no breakpoint is found") {
        fail()
      }
    }

    describe("#getLineBreakpoint") {
      it("should return the breakpoint bundle representing the line") {
        fail()
      }

      it("should throw an exception if no breakpoint is found") {
        fail()
      }
    }

    describe("#removeLineBreakpoint") {
      it("should return true if the breakpoint was successfully deleted") {
        fail()
      }

      it("should delete each breakpoint request represented by the bundle") {
        fail()
      }

      it("should return false if the breakpoint was not found") {
        fail()
      }
    }
  }
}
