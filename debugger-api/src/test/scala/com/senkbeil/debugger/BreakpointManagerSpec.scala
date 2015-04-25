package com.senkbeil.debugger

import org.scalatest.{Matchers, FunSpec}

class BreakpointManagerSpec extends FunSpec with Matchers {
  describe("BreakpointManager") {
    describe("#breakpointList") {
      it("should return a collection of breakpoint file names and lines") {

      }
    }

    describe("#setLineBreakpoint") {
      it("should throw an exception if the line is not available") {

      }

      it("should return true if successfully added the breakpoint") {

      }

      it("should create a new breakpoint request for each matching location") {

      }

      it("should return false if unable to create one of the underlying breakpoint requests") {

      }
    }

    describe("#hasLineBreakpoint") {
      it("should return true if the breakpoint with matching file name and line is found") {

      }

      it("should return false if no breakpoint is found") {

      }
    }

    describe("#getLineBreakpoint") {
      it("should return the breakpoint bundle representing the line") {

      }

      it("should throw an exception if no breakpoint is found") {

      }
    }

    describe("#removeLineBreakpoint") {
      it("should return true if the breakpoint was successfully deleted") {

      }

      it("should delete each breakpoint request represented by the bundle") {

      }

      it("should return false if the breakpoint was not found") {

      }
    }
  }
}
