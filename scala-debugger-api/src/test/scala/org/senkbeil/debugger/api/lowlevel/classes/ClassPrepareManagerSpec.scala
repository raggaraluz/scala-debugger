package org.senkbeil.debugger.api.lowlevel.classes

import java.util.concurrent.atomic.AtomicInteger

import com.sun.jdi.request.{ClassPrepareRequest, EventRequest, EventRequestManager}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.requests.{JDIRequestArgument, JDIRequestProcessor}

import scala.util.{Failure, Success}

class ClassPrepareManagerSpec extends FunSpec with Matchers with MockFactory
  with OneInstancePerTest with org.scalamock.matchers.Matchers
{
  private val TestId = java.util.UUID.randomUUID().toString
  private val mockEventRequestManager = mock[EventRequestManager]

  private val classPrepareManager = new ClassPrepareManager(mockEventRequestManager) {
    override protected def newRequestId(): String = TestId
  }

  describe("ClassPrepareManager") {
    describe("#classPrepareRequestList") {
      it("should contain all class prepare requests in the form of id -> request stored in the manager") {
        val classPrepareRequests = Seq(
          java.util.UUID.randomUUID().toString,
          java.util.UUID.randomUUID().toString
        )

        // Create a ClassPrepareManager whose generated request ids match the list
        // above
        val classPrepareManager = new ClassPrepareManager(mockEventRequestManager) {
          private val counter = new AtomicInteger(0)
          override protected def newRequestId(): String = {
            classPrepareRequests(counter.getAndIncrement % classPrepareRequests.length)
          }
        }

        classPrepareRequests.foreach { case id =>
          (mockEventRequestManager.createClassPrepareRequest _).expects()
            .returning(stub[ClassPrepareRequest]).once()
          classPrepareManager.createClassPrepareRequest()
        }

        classPrepareManager.classPrepareRequestList should
          contain theSameElementsAs (classPrepareRequests)
      }
    }

    describe("#createClassPrepareRequest") {
      it("should create the class prepare request and return Success(id)") {
        val expected = Success(TestId)

        val mockClassPrepareRequest = mock[ClassPrepareRequest]
        (mockEventRequestManager.createClassPrepareRequest _).expects()
          .returning(mockClassPrepareRequest).once()

        // Should set enabled to true by default, and
        // set the suspend policy to vm level by default
        (mockClassPrepareRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockClassPrepareRequest.setEnabled _).expects(true).once()

        val actual = classPrepareManager.createClassPrepareRequest()
        actual should be (expected)
      }

      it("should return the exception if unable to create the request") {
        val expected = Failure(new Throwable)

        (mockEventRequestManager.createClassPrepareRequest _).expects()
          .throwing(expected.failed.get).once()

        val actual = classPrepareManager.createClassPrepareRequest()
        actual should be (expected)
      }
    }

    describe("#hasClassPrepareRequest") {
      it("should return true if it exists") {
        val expected = true

        (mockEventRequestManager.createClassPrepareRequest _).expects()
          .returning(stub[ClassPrepareRequest]).once()

        val id = classPrepareManager.createClassPrepareRequest().get

        val actual = classPrepareManager.hasClassPrepareRequest(id)
        actual should be (expected)
      }

      it("should return false if it does not exist") {
        val expected = false

        val actual = classPrepareManager.hasClassPrepareRequest(TestId)
        actual should be (expected)
      }
    }

    describe("#getClassPrepareRequest") {
      it("should return Some(ClassPrepareRequest) if found") {
        val expected = stub[ClassPrepareRequest]

        (mockEventRequestManager.createClassPrepareRequest _).expects()
          .returning(expected).once()

        val id = classPrepareManager.createClassPrepareRequest().get

        val actual = classPrepareManager.getClassPrepareRequest(id)
        actual should be (Some(expected))
      }

      it("should return None if not found") {
        val expected = None

        val actual = classPrepareManager.getClassPrepareRequest(TestId)
        actual should be (expected)
      }
    }

    describe("#getClassPrepareArguments") {
      it("should return Some(Seq(input args)) if found") {
        val expected = Seq(mock[JDIRequestArgument], mock[JDIRequestArgument])
        expected.foreach(a => {
          val mockRequestProcessor = mock[JDIRequestProcessor]
          (mockRequestProcessor.process _).expects(*)
            .onCall((er: EventRequest) => er).once()
          (a.toProcessor _).expects().returning(mockRequestProcessor).once()
        })

        (mockEventRequestManager.createClassPrepareRequest _).expects()
          .returning(stub[ClassPrepareRequest]).once()

        val id = classPrepareManager.createClassPrepareRequest(expected: _*).get

        val actual = classPrepareManager.getClassPrepareRequestArguments(id)
        actual should be (Some(expected))
      }

      it("should return None if not found") {
        val expected = None

        val actual = classPrepareManager.getClassPrepareRequestArguments(TestId)
        actual should be (expected)
      }
    }

    describe("#removeClassPrepareRequest") {
      it("should return true if the class prepare request was removed") {
        val expected = true
        val stubRequest = stub[ClassPrepareRequest]

        (mockEventRequestManager.createClassPrepareRequest _).expects()
          .returning(stubRequest).once()

        val id = classPrepareManager.createClassPrepareRequest().get

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubRequest).once()

        val actual = classPrepareManager.removeClassPrepareRequest(id)
        actual should be (expected)
      }

      it("should return false if the class prepare request was not removed") {
        val expected = false

        val actual = classPrepareManager.removeClassPrepareRequest(TestId)
        actual should be (expected)
      }
    }
  }
}
