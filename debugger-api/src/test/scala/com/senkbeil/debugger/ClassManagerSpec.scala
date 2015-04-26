package com.senkbeil.debugger

import org.scalatest.{Matchers, FunSpec}

class ClassManagerSpec extends FunSpec with Matchers {
  describe("ClassManager") {
    describe("#linesAndLocationsForClass") {
      it("should return a map whose keys represent available lines") {
        fail()
      }

      it("should return a map whose values correspond to available locations per line") {
        fail()
      }

      it("should throw an exception if the class is not found in the cache") {
        fail()
      }
    }

    describe("#underlyingReferencesFor") {
      it("should return a collection of reference types matching the class found in the cache") {
        fail()
      }

      it("should throw an exception if the class is not found in the cache") {
        fail()
      }
    }

    describe("#allClassNames") {
      it("should refresh the classes and references") {
        fail()
      }

      it("should not refresh the classes and references if refresh == false") {
        fail()
      }

      it("should retrieve only Scala classes") {
        fail()
      }

      it("should return a collection of Scala class names") {
        fail()
      }
    }
  }
}
