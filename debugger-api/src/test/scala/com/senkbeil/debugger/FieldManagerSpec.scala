package com.senkbeil.debugger

import org.scalatest.{Matchers, FunSpec}

class FieldManagerSpec extends FunSpec with Matchers {
  describe("FieldManager") {
    describe("#staticFieldsForClass") {
      it("should throw an exception if the class is not found") {
        fail()
      }

      it("should return a collection of fields whose values can be retrieved") {
        fail()
      }

      it("should skip references whose fields cannot be retrieved") {
        fail()
      }

      it("should skip fields whose values cannot be retrieved") {
        fail()
      }
    }
  }
}
