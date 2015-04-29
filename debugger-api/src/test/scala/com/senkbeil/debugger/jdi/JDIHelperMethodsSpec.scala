package com.senkbeil.debugger.jdi

import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}

class JDIHelperMethodsSpec extends FunSpec with Matchers
  with OneInstancePerTest
{
  describe("JDIHelperMethods") {
    describe("#suspendVirtualMachineAndExecute") {
      it("should suspend the virtual machine while executing the code") {
        fail()
      }

      it("should resume the virtual machine after executing the code") {
        fail()
      }

      it("should return the results of executing the code") {
        fail()
      }

      it("should wrap the exceptions in a Try when executing the code") {
        fail()
      }
    }

    describe("#findMainMethod") {
      it("should return the thread reference to the main thread") {
        fail()
      }

      it("should throw an exception if no main thread can be found") {
        fail()
      }
    }

    describe("#fullOriginalClassName") {
      it("should get the original class name of the reference object") {
        fail()
      }

      it("should throw an exception if the refrence object comes from multiple sources") {
        fail()
      }
    }
  }
}
