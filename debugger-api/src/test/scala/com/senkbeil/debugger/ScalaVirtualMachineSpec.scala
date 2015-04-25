package com.senkbeil.debugger

import org.scalatest.{Matchers, FunSpec}

class ScalaVirtualMachineSpec extends FunSpec with Matchers {
  describe("ScalaVirtualMachine") {
    describe("#availableLinesForClass") {
      it("should return the lines (sorted) that can have breakpoints") {

      }

      it("should throw an exception if the class does not exist") {

      }
    }

    describe("#mainClassName") {
      it("should throw an exception if unable to find a main method") {

      }

      it("should throw an exception if encountered while getting the name") {

      }

      it("should return the name of the class containing the main method") {

      }
    }

    describe("#commandLineArguments") {
      it("should provide a list of string arguments provided to the JVM") {

      }
    }
  }
}
