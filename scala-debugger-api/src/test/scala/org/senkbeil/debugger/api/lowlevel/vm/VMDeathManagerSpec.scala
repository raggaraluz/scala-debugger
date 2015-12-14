package org.senkbeil.debugger.api.lowlevel.vm

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import test.TestVMDeathManager

import scala.util.Success

class VMDeathManagerSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockVMDeathManager = mock[VMDeathManager]
  private val testVMDeathManager = new TestVMDeathManager(
    mockVMDeathManager
  ) {
    override protected def newRequestId(): String = TestRequestId
  }

  describe("VMDeathManager") {
    describe("#createVMDeathRequest") {
      it("should invoke createVMDeathRequestWithId") {
        val expected = Success(TestRequestId)
        val testExtraArguments = Seq(stub[JDIRequestArgument])

        (mockVMDeathManager.createVMDeathRequestWithId _)
          .expects(TestRequestId, testExtraArguments)
          .returning(expected).once()

        val actual = testVMDeathManager.createVMDeathRequest(
          testExtraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#createVMDeathRequestFromInfo") {
      it("should invoke createVMDeathRequestWithId") {
        val expected = Success(TestRequestId)
        val testExtraArguments = Seq(stub[JDIRequestArgument])

        (mockVMDeathManager.createVMDeathRequestWithId _)
          .expects(TestRequestId, testExtraArguments)
          .returning(expected).once()

        val info = VMDeathRequestInfo(
          TestRequestId,
          testExtraArguments
        )
        val actual = testVMDeathManager.createVMDeathRequestFromInfo(info)

        actual should be(expected)
      }
    }
  }
}
