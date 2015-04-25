package com.senkbeil.debugger

import org.scalatest.{Matchers, FunSpec}

class DebuggerSpec extends FunSpec with Matchers {
  describe("Debugger") {
    describe("#isAvailable") {
      it("should return true if JDI is available") {

      }

      it("should return false if JDI is not available") {

      }
    }

    describe("#start") {
      it("should throw an exception if unable to load JDI") {

      }

      it("should throw an exception if unable to get the listening connector") {

      }

      it("should begin listening for virtual machine connections") {

      }
    }

    describe("#getVirtualMachines") {
      it("should return a list of connected virtual machines") {

      }
    }
  }
}
