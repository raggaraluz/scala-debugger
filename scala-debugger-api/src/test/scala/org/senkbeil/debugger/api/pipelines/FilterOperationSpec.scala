package org.senkbeil.debugger.api.pipelines

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}

class FilterOperationSpec extends FunSpec with Matchers with OneInstancePerTest
  with MockFactory
{
  describe("FilterOperation") {
    describe("#process") {
      it("should filter data to include only predicates resolving to true") {
        val expected = Seq(2, 4)

        val data = Seq(1, 2, 3, 4, 5)
        val operation = new FilterOperation[Int](_ % 2 == 0)
        val actual = operation.process(data)

        actual should be (expected)
      }
    }
  }
}
