package com.senkbeil.debugger.fields

import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}

class FieldManagerSpec extends FunSpec with Matchers with OneInstancePerTest {
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
