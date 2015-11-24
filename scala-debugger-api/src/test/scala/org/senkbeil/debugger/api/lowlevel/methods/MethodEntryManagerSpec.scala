package org.senkbeil.debugger.api.lowlevel.methods

import com.sun.jdi.request.{EventRequest, EventRequestManager, MethodEntryRequest}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}

import scala.util.{Failure, Success}

class MethodEntryManagerSpec extends FunSpec with Matchers with MockFactory
  with OneInstancePerTest with org.scalamock.matchers.Matchers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockEventRequestManager = mock[EventRequestManager]

  private val methodEntryManager = new MethodEntryManager(mockEventRequestManager) {
    override protected def newRequestId(): String = TestRequestId
  }

  describe("MethodEntryManager") {
    describe("#methodEntryRequestList") {
      it("should contain all method entry requests in the form of (class, method) stored in the manager") {
        val methodEntryRequests = Seq(
          ("class1", "method1"),
          ("class2", "method2")
        )

        methodEntryRequests.foreach { case (className, methodName) =>
          (mockEventRequestManager.createMethodEntryRequest _).expects()
            .returning(stub[MethodEntryRequest]).once()
          methodEntryManager.createMethodEntryRequest(className, methodName)
        }

        methodEntryManager.methodEntryRequestList should
          contain theSameElementsAs (methodEntryRequests)
      }
    }

    describe("#methodEntryRequestListById") {
      it("should contain all method entry request ids") {
        val methodEntryRequests = Seq(
          ("id1", "class1", "method1"),
          ("id2", "class2", "method2")
        )

        methodEntryRequests.foreach { case (requestId, className, methodName) =>
          (mockEventRequestManager.createMethodEntryRequest _).expects()
            .returning(stub[MethodEntryRequest]).once()
          methodEntryManager.createMethodEntryRequestWithId(
            requestId,
            className,
            methodName
          )
        }

        methodEntryManager.methodEntryRequestListById should
          contain theSameElementsAs (methodEntryRequests.map(_._1))
      }
    }

    describe("#createMethodEntryRequestWithId") {
      it("should create the method entry request using the provided id") {
        val expected = Success(java.util.UUID.randomUUID().toString)
        val testClassName = "some class name"
        val testMethodName = "some method name"

        val mockMethodEntryRequest = mock[MethodEntryRequest]
        (mockEventRequestManager.createMethodEntryRequest _).expects()
          .returning(mockMethodEntryRequest).once()

        // Should apply the class filter, set enabled to true by default, and
        // set the suspend policy to thread level by default
        (mockMethodEntryRequest.addClassFilter(_: String))
          .expects(testClassName).once()
        (mockMethodEntryRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockMethodEntryRequest.setEnabled _).expects(true).once()

        val actual = methodEntryManager.createMethodEntryRequestWithId(
          expected.get,
          testClassName,
          testMethodName
        )
        actual should be(expected)
      }
    }

    describe("#createMethodEntryRequest") {
      it("should create the method entry request with a class inclusion filter for the class name") {
        val expected = Success(TestRequestId)
        val testClassName = "some class name"
        val testMethodName = "some method name"

        val mockMethodEntryRequest = mock[MethodEntryRequest]
        (mockEventRequestManager.createMethodEntryRequest _).expects()
          .returning(mockMethodEntryRequest).once()

        // Should apply the class filter, set enabled to true by default, and
        // set the suspend policy to thread level by default
        (mockMethodEntryRequest.addClassFilter(_: String))
          .expects(testClassName).once()
        (mockMethodEntryRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockMethodEntryRequest.setEnabled _).expects(true).once()

        val actual = methodEntryManager.createMethodEntryRequest(testClassName, testMethodName)
        actual should be (expected)
      }

      it("should return the exception if unable to create the request") {
        val expected = Failure(new Throwable)
        val testClassName = "some class name"
        val testMethodName = "some method name"

        (mockEventRequestManager.createMethodEntryRequest _).expects()
          .throwing(expected.failed.get).once()

        val actual = methodEntryManager.createMethodEntryRequest(testClassName, testMethodName)
        actual should be (expected)
      }
    }

    describe("#hasMethodEntryRequestWithId") {
      it("should return true if it exists") {
        val expected = true

        val testClassName = "some class name"
        val testMethodName = "some method name"

        (mockEventRequestManager.createMethodEntryRequest _).expects()
          .returning(stub[MethodEntryRequest]).once()

        methodEntryManager.createMethodEntryRequestWithId(
          TestRequestId,
          testClassName,
          testMethodName
        )

        val actual = methodEntryManager.hasMethodEntryRequestWithId(TestRequestId)
        actual should be (expected)
      }

      it("should return false if it does not exist") {
        val expected = false

        val actual = methodEntryManager.hasMethodEntryRequestWithId(TestRequestId)
        actual should be (expected)
      }
    }

    describe("#hasMethodEntryRequest") {
      it("should return true if it exists") {
        val expected = true

        val testClassName = "some class name"
        val testMethodName = "some method name"

        (mockEventRequestManager.createMethodEntryRequest _).expects()
          .returning(stub[MethodEntryRequest]).once()

        methodEntryManager.createMethodEntryRequest(testClassName, testMethodName)

        val actual = methodEntryManager.hasMethodEntryRequest(testClassName, testMethodName)
        actual should be (expected)
      }

      it("should return false if it does not exist") {
        val expected = false

        val testClassName = "some class name"
        val testMethodName = "some method name"

        val actual = methodEntryManager.hasMethodEntryRequest(testClassName, testMethodName)
        actual should be (expected)
      }
    }

    describe("#getMethodEntryRequestWithId") {
      it("should return Some(MethodEntryRequest) if found") {
        val expected = stub[MethodEntryRequest]

        val testClassName = "some class name"
        val testMethodName = "some method name"

        (mockEventRequestManager.createMethodEntryRequest _).expects()
          .returning(expected).once()

        methodEntryManager.createMethodEntryRequestWithId(
          TestRequestId,
          testClassName,
          testMethodName
        )

        val actual = methodEntryManager.getMethodEntryRequestWithId(TestRequestId)
        actual should be (Some(expected))
      }

      it("should return None if not found") {
        val expected = None

        val actual = methodEntryManager.getMethodEntryRequestWithId(TestRequestId)
        actual should be (expected)
      }
    }

    describe("#getMethodEntryRequest") {
      it("should return Some(MethodEntryRequest) if found") {
        val expected = stub[MethodEntryRequest]

        val testClassName = "some class name"
        val testMethodName = "some method name"

        (mockEventRequestManager.createMethodEntryRequest _).expects()
          .returning(expected).once()

        methodEntryManager.createMethodEntryRequest(testClassName, testMethodName)

        val actual =
          methodEntryManager.getMethodEntryRequest(testClassName, testMethodName)
        actual should be (Some(expected))
      }

      it("should return None if not found") {
        val expected = None

        val testClassName = "some class name"
        val testMethodName = "some method name"

        val actual =
          methodEntryManager.getMethodEntryRequest(testClassName, testMethodName)
        actual should be (expected)
      }
    }

    describe("#removeMethodEntryRequestWithId") {
      it("should return true if the method entry request was removed") {
        val expected = true
        val stubRequest = stub[MethodEntryRequest]

        val testClassName = "some class name"
        val testMethodName = "some method name"

        (mockEventRequestManager.createMethodEntryRequest _).expects()
          .returning(stubRequest).once()

        methodEntryManager.createMethodEntryRequestWithId(
          TestRequestId,
          testClassName,
          testMethodName
        )

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubRequest).once()

        val actual =
          methodEntryManager.removeMethodEntryRequestWithId(TestRequestId)
        actual should be (expected)
      }

      it("should return false if the method entry request was not removed") {
        val expected = false

        val testClassName = "some class name"
        val testMethodName = "some method name"

        val actual =
          methodEntryManager.removeMethodEntryRequestWithId(TestRequestId)
        actual should be (expected)
      }
    }

    describe("#removeMethodEntryRequest") {
      it("should return true if the method entry request was removed") {
        val expected = true
        val stubRequest = stub[MethodEntryRequest]

        val testClassName = "some class name"
        val testMethodName = "some method name"

        (mockEventRequestManager.createMethodEntryRequest _).expects()
          .returning(stubRequest).once()

        methodEntryManager.createMethodEntryRequest(testClassName, testMethodName)

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubRequest).once()

        val actual =
          methodEntryManager.removeMethodEntryRequest(testClassName, testMethodName)
        actual should be (expected)
      }

      it("should return false if the method entry request was not removed") {
        val expected = false

        val testClassName = "some class name"
        val testMethodName = "some method name"

        val actual =
          methodEntryManager.removeMethodEntryRequest(testClassName, testMethodName)
        actual should be (expected)
      }
    }
  }
}
