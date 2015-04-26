package com.senkbeil.debugger

import org.scalatest.{Matchers, FunSpec}

class JDILoaderSpec extends FunSpec with Matchers {
  describe("JDILoader") {
    describe("#isJdiAvailable") {
      it("should return false if JDI is not available") {
        fail()
      }

      it("should return true if JDI is available") {
        fail()
      }
    }

    describe("#tryLoadJdi") {
      it("should return true if JDI can already be loaded") {
        fail()
      }

      it("should return false if no valid classloader can be found to load it") {
        fail()
      }

      it("should return false if could not add a valid classloader to system") {
        fail()
      }

      it("should return true if could add valid classloader to system") {
        fail()
      }
    }
  }
}
