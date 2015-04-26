package com.senkbeil.debugger

import org.scalatest.{Matchers, FunSpec}

class ScalaVirtualMachineSpec extends FunSpec with Matchers {
  describe("ScalaVirtualMachine") {
    describe("#availableLinesForClass") {
      it("should return the lines (sorted) that can have breakpoints") {
        fail()
      }

      it("should throw an exception if the class does not exist") {
        fail()
      }
    }

    describe("#mainClassName") {
      it("should throw an exception if unable to find a main method") {
        fail()
      }

      it("should throw an exception if encountered while getting the name") {
        fail()
      }

      it("should return the name of the class containing the main method") {
        fail()
      }
    }

    describe("#commandLineArguments") {
      it("should provide a list of string arguments provided to the JVM") {
        fail()
      }
    }
  }
}
