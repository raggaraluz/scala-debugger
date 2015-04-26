package com.senkbeil.debugger

import com.sun.jdi.request.BreakpointRequest
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfter, Matchers, FunSpec}

class BreakpointBundleSpec extends FunSpec with Matchers with BeforeAndAfter
  with MockFactory
{
  private var breakpointRequests: Seq[BreakpointRequest] = _
  private var breakpointBundle: BreakpointBundle = _

  before {
    breakpointRequests = Seq(stub[BreakpointRequest], stub[BreakpointRequest])
    breakpointBundle = new BreakpointBundle(breakpointRequests)
  }

  describe("BreakpointBundle") {
    describe("#apply") {
      it("should return the breakpoint request at the specific index") {
        val expected = breakpointRequests.head
        val actual = breakpointBundle(0)

        actual should be (expected)
      }

      it("should throw an exception if the index is invalid") {
        val totalRequests = breakpointRequests.length

        // Access 1 past max range
        intercept[IndexOutOfBoundsException] {
          breakpointBundle(totalRequests)
        }
      }
    }

    describe("#length") {
      it("should return the total number of breakpoint requests contained") {
        fail()
      }
    }

    describe("#iterator") {
      it("should return an iterator over the contained breakpoint requests") {
        fail()
      }
    }

    describe("#addInstanceFilter") {
      it("should add the instance filter to each underlying breakpoint request") {
        fail()
      }
    }

    describe("#addThreadFilter") {
      it("should add the thread filter to each underlying breakpoint request") {
        fail()
      }
    }

    describe("#addCountFilter") {
      it("should add the count filter to each underlying breakpoint request") {
        fail()
      }
    }

    describe("#disable") {
      it("should disable each underlying breakpoint request") {
        fail()
      }
    }

    describe("#enable") {
      it("should enable each underlying breakpoint request") {
        fail()
      }
    }

    describe("#isEnabled") {
      it("should return true if all breakpoint requests are enabled") {
        fail()
      }

      it("should return false if all breakpoint requests are disabled") {
        fail()
      }

      it("should throw an exception if the breakpoint requests are out of sync") {
        fail()
      }
    }

    describe("#setEnabled") {
      it("should set the enable status of each underlying breakpoint request") {
        fail()
      }
    }

    describe("#getProperty") {
      it("should return the property contained by all breakpoint requests") {
        fail()
      }

      it("should throw an exception if the breakpoint requests are out of sync") {
        fail()
      }
    }

    describe("#putProperty") {
      it("should set the property for each underlying breakpoint request") {
        fail()
      }
    }

    describe("#setSuspendPolicy") {
      it("should set the suspend policy for each underlying breakpoint request") {
        fail()
      }
    }

    describe("#suspendPolicy") {
      it("should return the suspend policy matching all of the underlying breakpoint requests") {
        fail()
      }

      it("should throw an exception if the breakpoint requests are out of sync") {
        fail()
      }
    }

    describe("#virtualMachine") {
      it("should return the virtual machine where all of the underlying breakpoint requests reside") {
        fail()
      }

      it("should throw an exception if the breakpoint requests are out of sync") {
        fail()
      }
    }
  }
}
