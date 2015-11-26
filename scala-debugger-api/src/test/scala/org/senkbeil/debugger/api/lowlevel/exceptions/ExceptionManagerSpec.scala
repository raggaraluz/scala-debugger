package org.senkbeil.debugger.api.lowlevel.exceptions

import com.sun.jdi.{ReferenceType, VirtualMachine}
import com.sun.jdi.request.{EventRequest, EventRequestManager, ExceptionRequest}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import scala.collection.JavaConverters._
import scala.util.{Failure, Success}

class ExceptionManagerSpec extends FunSpec with Matchers with MockFactory
  with OneInstancePerTest with org.scalamock.matchers.Matchers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockReferenceType = mock[ReferenceType]
  private val mockEventRequestManager = mock[EventRequestManager]

  private val mockVirtualMachine = mock[VirtualMachine]

  // Use same mock reference type for any request to retrieve classes by name
  (mockVirtualMachine.classesByName _).expects(*)
    .returning(Seq(mockReferenceType).toList.asJava).anyNumberOfTimes()

  private val exceptionManager = new ExceptionManager(
    mockVirtualMachine,
    mockEventRequestManager
  ) {
    override protected def newRequestId(): String = TestRequestId
  }

  describe("ExceptionManager") {
    describe("#createCatchallExceptionRequestWithId") {
      it("should create the exception request using the provided id") {
        val expected = Success(java.util.UUID.randomUUID().toString)
        val testNotifyCaught = true
        val testNotifyUncaught = false

        val mockExceptionRequest = mock[ExceptionRequest]
        (mockEventRequestManager.createExceptionRequest _)
          .expects(null, testNotifyCaught, testNotifyUncaught)
          .returning(mockExceptionRequest).once()

        (mockExceptionRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockExceptionRequest.setEnabled _).expects(true).once()

        val actual = exceptionManager.createCatchallExceptionRequestWithId(
          expected.get,
          testNotifyCaught,
          testNotifyUncaught
        )
        actual should be(expected)
      }

      it("should add the exception request id to the standard exception request list") {
        val expected = java.util.UUID.randomUUID().toString
        val testNotifyCaught = true
        val testNotifyUncaught = false

        val mockExceptionRequest = mock[ExceptionRequest]
        (mockEventRequestManager.createExceptionRequest _)
          .expects(null, testNotifyCaught, testNotifyUncaught)
          .returning(mockExceptionRequest).once()

        (mockExceptionRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockExceptionRequest.setEnabled _).expects(true).once()

        exceptionManager.createCatchallExceptionRequestWithId(
          expected,
          testNotifyCaught,
          testNotifyUncaught
        )

        exceptionManager.exceptionRequestListById should have length 1

        val actual = exceptionManager.exceptionRequestListById.head
        actual should be(expected)
      }
    }

    describe("#createCatchallExceptionRequest") {
      it("should create the exception request and return Success(id)") {
        val expected = Success(TestRequestId)
        val testNotifyCaught = true
        val testNotifyUncaught = false

        val mockExceptionRequest = mock[ExceptionRequest]
        (mockEventRequestManager.createExceptionRequest _)
          .expects(null, testNotifyCaught, testNotifyUncaught)
          .returning(mockExceptionRequest).once()

        (mockExceptionRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockExceptionRequest.setEnabled _).expects(true).once()

        val actual = exceptionManager.createCatchallExceptionRequest(
          testNotifyCaught,
          testNotifyUncaught
        )
        actual should be (expected)
      }

      it("should return the exception if failed to create the exception request") {
        val expected = Failure(new Throwable)
        val testNotifyCaught = true
        val testNotifyUncaught = false

        (mockEventRequestManager.createExceptionRequest _)
          .expects(null, testNotifyCaught, testNotifyUncaught)
          .throwing(expected.failed.get).once()

        val actual = exceptionManager.createCatchallExceptionRequest(
          testNotifyCaught,
          testNotifyUncaught
        )
        actual should be (expected)
      }
    }

    describe("#hasCatchallExceptionRequest") {
      it("should return true if the catchall has been set") {
        val expected = true

        (mockEventRequestManager.createExceptionRequest _)
          .expects(*, *, *)
          .returning(stub[ExceptionRequest]).once()

        exceptionManager.createCatchallExceptionRequest(true, true) should
          be (Success(TestRequestId))

        val actual = exceptionManager.hasCatchallExceptionRequest
        actual should be (expected)
      }

      it("should return false if the catchall has not been set") {
        val expected = false

        val actual = exceptionManager.hasCatchallExceptionRequest
        actual should be (expected)
      }
    }

    describe("#getCatchallExceptionRequestId") {
      it("should return Some(id) if the catchall has been set") {
        val expected = Some(TestRequestId)

        (mockEventRequestManager.createExceptionRequest _)
          .expects(*, *, *)
          .returning(stub[ExceptionRequest]).once()

        exceptionManager.createCatchallExceptionRequest(true, true) should
          be (Success(expected.get))

        val actual = exceptionManager.getCatchallExceptionRequestId
        actual should be (expected)
      }

      it("should return None if the catchall has not been set") {
        val expected = None

        val actual = exceptionManager.getCatchallExceptionRequestId
        actual should be (expected)
      }
    }

    describe("#getCatchallExceptionRequest") {
      it("should return Some(ExceptionRequest) if the catchall has been set") {
        val expected = Some(stub[ExceptionRequest])

        (mockEventRequestManager.createExceptionRequest _)
          .expects(*, *, *)
          .returning(expected.get).once()

        exceptionManager.createCatchallExceptionRequest(true, true) should
          be (Success(TestRequestId))

        val actual = exceptionManager.getCatchallExceptionRequest
        actual should be (expected)
      }

      it("should return None if the catchall has not been set") {
        val expected = None

        val actual = exceptionManager.getCatchallExceptionRequest
        actual should be (expected)
      }
    }

    describe("#removeCatchallExceptionRequest") {
      it("should return true if the exception request was removed") {
        val expected = true

        val stubExceptionRequest = stub[ExceptionRequest]

        (mockEventRequestManager.createExceptionRequest _)
          .expects(*, *, *)
          .returning(stubExceptionRequest).once()

        exceptionManager.createCatchallExceptionRequest(true, true) should
          be (Success(TestRequestId))

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubExceptionRequest).once()

        val actual = exceptionManager.removeCatchallExceptionRequest()
        actual should be (expected)
      }

      it("should return false if the exception request was not removed") {
        val expected = false

        val actual = exceptionManager.removeCatchallExceptionRequest()
        actual should be (expected)
      }
    }

    describe("#exceptionRequestListById") {
      it("should contain all exception requests in the form of (class, method) stored in the manager") {
        val testNotifyCaught = true
        val testNotifyUncaught = false
        val exceptionRequests = Seq(("id1", "class1"), ("id2", "class2"))

        exceptionRequests.foreach { case (requestId, exceptionName) =>
          (mockEventRequestManager.createExceptionRequest _)
            .expects(mockReferenceType, testNotifyCaught, testNotifyUncaught)
            .returning(stub[ExceptionRequest]).once()
          exceptionManager.createExceptionRequestWithId(
            requestId,
            exceptionName,
            testNotifyCaught,
            testNotifyUncaught
          )
        }

        exceptionManager.exceptionRequestListById should
          contain theSameElementsAs (exceptionRequests.map(_._1))
      }
    }

    describe("#exceptionRequestList") {
      it("should contain all exception requests in the form of (class, method) stored in the manager") {
        val testNotifyCaught = true
        val testNotifyUncaught = false
        val exceptionRequests = Seq("class1", "class2")

        exceptionRequests.foreach { case exceptionName =>
          (mockEventRequestManager.createExceptionRequest _)
            .expects(mockReferenceType, testNotifyCaught, testNotifyUncaught)
            .returning(stub[ExceptionRequest]).once()
          exceptionManager.createExceptionRequest(
            exceptionName,
            testNotifyCaught,
            testNotifyUncaught
          )
        }

        exceptionManager.exceptionRequestList should
          contain theSameElementsAs (exceptionRequests)
      }
    }

    describe("#createExceptionRequestWithId") {
      it("should create the exception request using the provided id") {
        val expected = Success(java.util.UUID.randomUUID().toString)
        val testExceptionName = "some.exception.name"
        val testNotifyCaught = true
        val testNotifyUncaught = false

        val mockExceptionRequest = mock[ExceptionRequest]
        (mockEventRequestManager.createExceptionRequest _)
          .expects(mockReferenceType, testNotifyCaught, testNotifyUncaught)
          .returning(mockExceptionRequest).once()

        (mockExceptionRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockExceptionRequest.setEnabled _).expects(true).once()

        val actual = exceptionManager.createExceptionRequestWithId(
          expected.get,
          testExceptionName,
          testNotifyCaught,
          testNotifyUncaught
        )
        actual should be(expected)
      }
    }

    describe("#createExceptionRequest") {
      it("should create the exception request and return Success(id)") {
        val expected = Success(TestRequestId)
        val testExceptionName = "some.exception.name"
        val testNotifyCaught = true
        val testNotifyUncaught = false

        val mockExceptionRequest = mock[ExceptionRequest]
        (mockEventRequestManager.createExceptionRequest _)
          .expects(mockReferenceType, testNotifyCaught, testNotifyUncaught)
          .returning(mockExceptionRequest).once()

        (mockExceptionRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockExceptionRequest.setEnabled _).expects(true).once()

        val actual = exceptionManager.createExceptionRequest(
          testExceptionName,
          testNotifyCaught,
          testNotifyUncaught
        )
        actual should be (expected)
      }

      it("should return a custom exception if no class is found for the request") {
        val testExceptionName = "some.exception.name"
        val expected = Failure(NoExceptionClassFound(testExceptionName))
        val testNotifyCaught = true
        val testNotifyUncaught = false

        // Set up the virtual machine to not return any classes when asked
        val mockVirtualMachine = mock[VirtualMachine]
        (mockVirtualMachine.classesByName _).expects(*)
          .returning(Seq[ReferenceType]().toList.asJava).once()

        val exceptionManager = new ExceptionManager(
          mockVirtualMachine,
          mockEventRequestManager
        )

        val actual = exceptionManager.createExceptionRequest(
          testExceptionName,
          testNotifyCaught,
          testNotifyUncaught
        )
        actual should be (expected)
      }

      it("should return the exception if failed to create the exception request") {
        val expected = Failure(new Throwable)
        val testNotifyCaught = true
        val testNotifyUncaught = false
        val testExceptionName = "some exception name"

        (mockEventRequestManager.createExceptionRequest _)
          .expects(mockReferenceType, testNotifyCaught, testNotifyUncaught)
          .throwing(expected.failed.get).once()

        val actual = exceptionManager.createExceptionRequest(
          testExceptionName,
          testNotifyCaught,
          testNotifyUncaught
        )
        actual should be (expected)
      }
    }

    describe("#hasExceptionRequestWithId") {
      it("should return true if it exists") {
        val expected = true

        val testNotifyCaught = true
        val testNotifyUncaught = false
        val testExceptionName = "some exception name"

        (mockEventRequestManager.createExceptionRequest _)
          .expects(mockReferenceType, testNotifyCaught, testNotifyUncaught)
          .returning(stub[ExceptionRequest]).once()

        exceptionManager.createExceptionRequestWithId(
          TestRequestId,
          testExceptionName,
          testNotifyCaught,
          testNotifyUncaught
        )

        val actual = exceptionManager.hasExceptionRequestWithId(TestRequestId)
        actual should be (expected)
      }

      it("should return false if it does not exist") {
        val expected = false

        val actual = exceptionManager.hasExceptionRequestWithId(TestRequestId)
        actual should be (expected)
      }
    }

    describe("#hasExceptionRequest") {
      it("should return true if it exists") {
        val expected = true

        val testNotifyCaught = true
        val testNotifyUncaught = false
        val testExceptionName = "some exception name"

        (mockEventRequestManager.createExceptionRequest _)
          .expects(mockReferenceType, testNotifyCaught, testNotifyUncaught)
          .returning(stub[ExceptionRequest]).once()

        exceptionManager.createExceptionRequest(
          testExceptionName,
          testNotifyCaught,
          testNotifyUncaught
        )

        val actual = exceptionManager.hasExceptionRequest(testExceptionName)
        actual should be (expected)
      }

      it("should return false if it does not exist") {
        val expected = false

        val testExceptionName = "some exception name"

        val actual = exceptionManager.hasExceptionRequest(testExceptionName)
        actual should be (expected)
      }
    }

    describe("#getExceptionRequestWithId") {
      it("should return Some(Seq(ExceptionRequest)) if found") {
        val expected = Seq(stub[ExceptionRequest])

        val testNotifyCaught = true
        val testNotifyUncaught = false
        val testExceptionName = "some exception name"

        (mockEventRequestManager.createExceptionRequest _)
          .expects(mockReferenceType, testNotifyCaught, testNotifyUncaught)
          .returning(expected.head).once()

        exceptionManager.createExceptionRequestWithId(
          TestRequestId,
          testExceptionName,
          testNotifyCaught,
          testNotifyUncaught
        )

        val actual = exceptionManager.getExceptionRequestWithId(TestRequestId).get
        actual should contain theSameElementsAs (expected)
      }

      it("should return None if not found") {
        val expected = None

        val actual = exceptionManager.getExceptionRequestWithId(TestRequestId)
        actual should be (expected)
      }
    }

    describe("#getExceptionRequest") {
      it("should return Some(Seq(ExceptionRequest)) if found") {
        val expected = Seq(stub[ExceptionRequest])

        val testNotifyCaught = true
        val testNotifyUncaught = false
        val testExceptionName = "some exception name"

        (mockEventRequestManager.createExceptionRequest _)
          .expects(mockReferenceType, testNotifyCaught, testNotifyUncaught)
          .returning(expected.head).once()

        exceptionManager.createExceptionRequest(
          testExceptionName,
          testNotifyCaught,
          testNotifyUncaught
        )

        val actual = exceptionManager.getExceptionRequest(testExceptionName).get
        actual should contain theSameElementsAs (expected)
      }

      it("should return None if not found") {
        val expected = None

        val testExceptionName = "some exception name"

        val actual = exceptionManager.getExceptionRequest(testExceptionName)
        actual should be (expected)
      }
    }

    describe("#removeExceptionRequestWithId") {
      it("should return true if the catchall exception request was removed") {
        val expected = true
        val stubRequest = stub[ExceptionRequest]

        val testNotifyCaught = true
        val testNotifyUncaught = false

        (mockEventRequestManager.createExceptionRequest _)
          .expects(null, testNotifyCaught, testNotifyUncaught)
          .returning(stubRequest).once()

        exceptionManager.createCatchallExceptionRequestWithId(
          TestRequestId,
          testNotifyCaught,
          testNotifyUncaught
        )

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubRequest).once()

        val actual = exceptionManager.removeExceptionRequestWithId(TestRequestId)
        actual should be (expected)
      }

      it("should return true if the exception request was removed") {
        val expected = true
        val stubRequest = stub[ExceptionRequest]

        val testNotifyCaught = true
        val testNotifyUncaught = false
        val testExceptionName = "some exception name"

        (mockEventRequestManager.createExceptionRequest _)
          .expects(mockReferenceType, testNotifyCaught, testNotifyUncaught)
          .returning(stubRequest).once()

        exceptionManager.createExceptionRequestWithId(
          TestRequestId,
          testExceptionName,
          testNotifyCaught,
          testNotifyUncaught
        )

        (mockEventRequestManager.deleteEventRequests _)
          .expects(Seq(stubRequest).asJava).once()

        val actual = exceptionManager.removeExceptionRequestWithId(TestRequestId)
        actual should be (expected)
      }

      it("should return false if the exception request was not removed") {
        val expected = false

        val actual = exceptionManager.removeExceptionRequestWithId(TestRequestId)
        actual should be (expected)
      }
    }

    describe("#removeExceptionRequest") {
      it("should return true if the exception request was removed") {
        val expected = true
        val stubRequest = stub[ExceptionRequest]

        val testNotifyCaught = true
        val testNotifyUncaught = false
        val testExceptionName = "some exception name"

        (mockEventRequestManager.createExceptionRequest _)
          .expects(mockReferenceType, testNotifyCaught, testNotifyUncaught)
          .returning(stubRequest).once()

        exceptionManager.createExceptionRequest(
          testExceptionName,
          testNotifyCaught,
          testNotifyUncaught
        )

        (mockEventRequestManager.deleteEventRequests _)
          .expects(Seq(stubRequest).asJava).once()

        val actual = exceptionManager.removeExceptionRequest(testExceptionName)
        actual should be (expected)
      }

      it("should return false if the exception request was not removed") {
        val expected = false

        val testExceptionName = "some exception name"

        val actual = exceptionManager.removeExceptionRequest(testExceptionName)
        actual should be (expected)
      }
    }
  }
}
