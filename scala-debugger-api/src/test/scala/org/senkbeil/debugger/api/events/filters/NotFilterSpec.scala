package org.senkbeil.debugger.api.events.filters

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}

class NotFilterSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{
  private val notFilter = NotFilter(mock[JDIEventFilter])

  describe("NotFilter") {
    describe("#toProcessor") {
      it("should return a processor containing the or filter") {
        notFilter.toProcessor.argument should be (notFilter)
      }
    }
  }
}
