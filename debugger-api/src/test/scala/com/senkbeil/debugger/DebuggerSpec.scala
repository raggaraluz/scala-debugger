package com.senkbeil.debugger

import org.scalatest.{Matchers, FunSpec}

class DebuggerSpec extends FunSpec with Matchers {
  describe("Debugger") {
    describe("#isAvailable") {
      it("should return true if jdi loader is available") {
        fail()
      }

      it("should return false if jdi loader is not available") {
        fail()
      }
    }

    describe("#assertJdiLoaded") {
      it("should throw an assertion error if failed to load jdi") {
        fail()
      }

      it("should do nothing if successfully loaded jdi") {
        fail()
      }
    }
  }
}
