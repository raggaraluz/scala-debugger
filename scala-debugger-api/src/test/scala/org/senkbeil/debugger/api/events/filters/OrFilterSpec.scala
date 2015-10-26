package org.senkbeil.debugger.api.events.filters

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}

class OrFilterSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{
  private val orFilter = OrFilter()

  describe("OrFilter") {
    describe("#toProcessor") {
      it("should return a processor containing the or filter") {
        orFilter.toProcessor.argument should be (orFilter)
      }
    }
  }
}
