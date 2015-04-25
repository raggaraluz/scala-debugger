package com.senkbeil.debugger

import org.scalatest.{Matchers, FunSpec}

class ClassManagerSpec extends FunSpec with Matchers {
  describe("ClassManager") {
    describe("#linesAndLocationsForClass") {
      it("should return a map whose keys represent available lines") {

      }

      it("should return a map whose values correspond to available locations per line") {

      }

      it("should throw an exception if the class is not found in the cache") {

      }
    }

    describe("#underlyingReferencesFor") {
      it("should return a collection of reference types matching the class found in the cache") {

      }

      it("should throw an exception if the class is not found in the cache") {

      }
    }

    describe("#allClassNames") {
      it("should refresh the classes and references") {

      }

      it("should not refresh the classes and references if refresh == false") {

      }

      it("should retrieve only Scala classes") {

      }

      it("should return a collection of Scala class names") {

      }
    }
  }
}
