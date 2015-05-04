package com.senkbeil.debugger

import org.scalatest.{OneInstancePerTest, Matchers, FunSpec}

class LaunchingDebuggerSpec extends FunSpec with Matchers
  with OneInstancePerTest
{
  describe("LaunchingDebugger") {
    describe("#start") {
      it("should throw an exception if unable to load JDI") {
        fail()
      }

      it("should throw an exception if already started") {
        fail()
      }

      it("should throw an exception if unable to get the launching connector") {
        fail()
      }

      it("should spawn a new process using the provided arguments and class") {
        fail()
      }
    }

    describe("#stop") {
      it("should throw an exception if not started") {
        fail()
      }

      it("should stop the running JVM process") {
        fail()
      }
    }
  }
}
