package org.senkbeil.debugger.api.pipelines

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}

class CloseOperationSpec extends FunSpec with Matchers with OneInstancePerTest
  with MockFactory
{
  describe("CloseOperation") {
    describe("#process") {
      it("should invoke the constructor-provided close function") {
        val mockCloseFunction = mockFunction[Unit]
        val operation = new CloseOperation[Int](mockCloseFunction)

        mockCloseFunction.expects().once()

        operation.process(Seq(1, 2, 3)) should be (empty)
      }
    }
  }
}
