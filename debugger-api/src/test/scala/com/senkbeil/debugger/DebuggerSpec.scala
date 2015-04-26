package com.senkbeil.debugger

import org.scalatest.{Matchers, FunSpec}

class DebuggerSpec extends FunSpec with Matchers {
  describe("Debugger") {
    describe("#isAvailable") {
      it("should return true if JDI is available") {
        fail()
      }

      it("should return false if JDI is not available") {
        fail()
      }
    }

    describe("#start") {
      it("should throw an exception if unable to load JDI") {
        fail()
      }

      it("should throw an exception if unable to get the listening connector") {
        fail()
      }

      it("should begin listening for virtual machine connections") {
        fail()
      }
    }

    describe("#getVirtualMachines") {
      it("should return a list of connected virtual machines") {
        fail()
      }
    }
  }
}
