package com.senkbeil.debugger

import org.scalatest.{OneInstancePerTest, Matchers, FunSpec}

class ListeningDebuggerSpec extends FunSpec with Matchers
  with OneInstancePerTest
{
  describe("ListeningDebugger") {
    describe("#start") {
      it("should throw an exception if unable to load JDI") {
        fail()
      }

      it("should throw an exception if already started") {
        fail()
      }

      it("should throw an exception if unable to get the listening connector") {
        fail()
      }

      it("should begin listening for virtual machine connections") {
        fail()
      }

      it("should create an executor service using the provided function") {
        fail()
      }

      it("should spawn X workers to listen for incoming JVM connections") {
        fail()
      }
    }

    describe("#stop") {
      it("should throw an exception if not started") {
        fail()
      }

      it("should stop listening for incoming JVM connections") {
        fail()
      }

      it("should shutdown all workers listening for incoming connections") {
        fail()
      }
    }

    describe("#connectedVirtualMachines") {
      it("should return a collection of connected virtual machines") {
        fail()
      }
    }
  }
}
