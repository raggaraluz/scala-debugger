package org.senkbeil.debugger.api.lowlevel.classes

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import test.TestClassUnloadManager

import scala.util.Success

class ClassUnloadManagerSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockClassUnloadManager = mock[ClassUnloadManager]
  private val testClassUnloadManager = new TestClassUnloadManager(
    mockClassUnloadManager
  ) {
    override protected def newRequestId(): String = TestRequestId
  }

  describe("ClassUnloadManager") {
    describe("#createClassUnloadRequest") {
      it("should invoke createClassUnloadRequestWithId") {
        val expected = Success(TestRequestId)
        val testExtraArguments = Seq(stub[JDIRequestArgument])

        (mockClassUnloadManager.createClassUnloadRequestWithId _)
          .expects(TestRequestId, testExtraArguments)
          .returning(expected).once()

        val actual = testClassUnloadManager.createClassUnloadRequest(
          testExtraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#createClassUnloadRequestFromInfo") {
      it("should invoke createClassUnloadRequestWithId") {
        val expected = Success(TestRequestId)
        val testExtraArguments = Seq(stub[JDIRequestArgument])

        (mockClassUnloadManager.createClassUnloadRequestWithId _)
          .expects(TestRequestId, testExtraArguments)
          .returning(expected).once()

        val info = ClassUnloadRequestInfo(
          TestRequestId,
          testExtraArguments
        )
        val actual = testClassUnloadManager.createClassUnloadRequestFromInfo(info)

        actual should be(expected)
      }
    }
  }
}
