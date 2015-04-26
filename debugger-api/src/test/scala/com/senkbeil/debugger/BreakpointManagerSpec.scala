package com.senkbeil.debugger

import org.scalatest.{Matchers, FunSpec}

class BreakpointManagerSpec extends FunSpec with Matchers {
  describe("BreakpointManager") {
    describe("#breakpointList") {
      it("should return a collection of breakpoint file names and lines") {
        fail()
      }
    }

    describe("#setLineBreakpoint") {
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
