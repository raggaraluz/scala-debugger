package com.senkbeil.debugger

import org.scalatest.{Matchers, FunSpec}

class BreakpointBundleSpec extends FunSpec with Matchers {
  describe("BreakpointBundle") {
    describe("#apply") {
      it("should return the breakpoint request at the specific index") {

      }

      it("should throw an exception if the index is invalid") {

      }
    }

    describe("#length") {
      it("should return the total number of breakpoint requests contained") {

      }
    }

    describe("#iterator") {
      it("should return an iterator over the contained breakpoint requests") {

      }
    }

    describe("#addInstanceFilter") {
      it("should add the instance filter to each underlying breakpoint request") {

      }
    }

    describe("#addThreadFilter") {
      it("should add the thread filter to each underlying breakpoint request") {

      }
    }

    describe("#addCountFilter") {
      it("should add the count filter to each underlying breakpoint request") {

      }
    }

    describe("#disable") {
      it("should disable each underlying breakpoint request") {

      }
    }

    describe("#enable") {
      it("should enable each underlying breakpoint request") {

      }
    }

    describe("#isEnabled") {
      it("should return true if all breakpoint requests are enabled") {

      }

      it("should return false if all breakpoint requests are disabled") {

      }

      it("should throw an exception if the breakpoint requests are out of sync") {

      }
    }

    describe("#setEnabled") {
      it("should set the enable status of each underlying breakpoint request") {

      }
    }

    describe("#getProperty") {
      it("should return the property contained by all breakpoint requests") {

      }

      it("should throw an exception if the breakpoint requests are out of sync") {

      }
    }

    describe("#putProperty") {
      it("should set the property for each underlying breakpoint request") {

      }
    }

    describe("#setSuspendPolicy") {
      it("should set the suspend policy for each underlying breakpoint request") {

      }
    }

    describe("#suspendPolicy") {
      it("should return the suspend policy matching all of the underlying breakpoint requests") {

      }

      it("should throw an exception if the breakpoint requests are out of sync") {

      }
    }

    describe("#virtualMachine") {
      it("should return the virtual machine where all of the underlying breakpoint requests reside") {

      }

      it("should throw an exception if the breakpoint requests are out of sync") {

      }
    }
  }
}
