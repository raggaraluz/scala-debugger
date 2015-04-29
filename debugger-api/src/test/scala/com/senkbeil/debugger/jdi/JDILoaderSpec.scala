package com.senkbeil.debugger.jdi

import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}

class JDILoaderSpec extends FunSpec with Matchers with OneInstancePerTest {
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
