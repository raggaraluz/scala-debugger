package org.senkbeil.debugger.api.lowlevel.threads

import java.util.concurrent.atomic.AtomicInteger

import com.sun.jdi.request.{EventRequest, EventRequestManager, ThreadStartRequest}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}

import scala.util.{Failure, Success}

class ThreadStartManagerSpec extends FunSpec with Matchers with MockFactory
  with OneInstancePerTest with org.scalamock.matchers.Matchers
{
  private val TestId = java.util.UUID.randomUUID().toString
  private val mockEventRequestManager = mock[EventRequestManager]

  private val threadStartManager = new ThreadStartManager(mockEventRequestManager) {
    override protected def newRequestId(): String = TestId
  }

  describe("ThreadStartManager") {
    describe("#threadStartRequestList") {
      it("should contain all thread start requests in the form of id -> request stored in the manager") {
        val threadStartRequests = Seq(
          java.util.UUID.randomUUID().toString,
          java.util.UUID.randomUUID().toString
        )

        // Create a ThreadStartManager whose generated request ids match the list
        // above
        val threadStartManager = new ThreadStartManager(mockEventRequestManager) {
          private val counter = new AtomicInteger(0)
          override protected def newRequestId(): String = {
            threadStartRequests(counter.getAndIncrement % threadStartRequests.length)
          }
        }

        threadStartRequests.foreach { case id =>
          (mockEventRequestManager.createThreadStartRequest _).expects()
            .returning(stub[ThreadStartRequest]).once()
          threadStartManager.createThreadStartRequest()
        }

        threadStartManager.threadStartRequestList should
          contain theSameElementsAs (threadStartRequests)
      }
    }

    describe("#createThreadStartRequest") {
      it("should create the thread start request and return Success(id)") {
        val expected = Success(TestId)

        val mockThreadStartRequest = mock[ThreadStartRequest]
        (mockEventRequestManager.createThreadStartRequest _).expects()
          .returning(mockThreadStartRequest).once()

        // Should set enabled to true by default, and
        // set the suspend policy to thread level by default
        (mockThreadStartRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockThreadStartRequest.setEnabled _).expects(true).once()

        val actual = threadStartManager.createThreadStartRequest()
        actual should be (expected)
      }

      it("should return the exception if unable to create the request") {
        val expected = Failure(new Throwable)

        (mockEventRequestManager.createThreadStartRequest _).expects()
          .throwing(expected.failed.get).once()

        val actual = threadStartManager.createThreadStartRequest()
        actual should be (expected)
      }
    }

    describe("#hasThreadStartRequest") {
      it("should return true if it exists") {
        val expected = true

        (mockEventRequestManager.createThreadStartRequest _).expects()
          .returning(stub[ThreadStartRequest]).once()

        val id = threadStartManager.createThreadStartRequest().get

        val actual = threadStartManager.hasThreadStartRequest(id)
        actual should be (expected)
      }

      it("should return false if it does not exist") {
        val expected = false

        val actual = threadStartManager.hasThreadStartRequest(TestId)
        actual should be (expected)
      }
    }

    describe("#getThreadStartRequest") {
      it("should return Some(ThreadStartRequest) if found") {
        val expected = stub[ThreadStartRequest]

        (mockEventRequestManager.createThreadStartRequest _).expects()
          .returning(expected).once()

        val id = threadStartManager.createThreadStartRequest().get

        val actual = threadStartManager.getThreadStartRequest(id)
        actual should be (Some(expected))
      }

      it("should return None if not found") {
        val expected = None

        val actual = threadStartManager.getThreadStartRequest(TestId)
        actual should be (expected)
      }
    }

    describe("#removeThreadStartRequest") {
      it("should return true if the thread start request was removed") {
        val expected = true
        val stubRequest = stub[ThreadStartRequest]

        (mockEventRequestManager.createThreadStartRequest _).expects()
          .returning(stubRequest).once()

        val id = threadStartManager.createThreadStartRequest().get

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubRequest).once()

        val actual = threadStartManager.removeThreadStartRequest(id)
        actual should be (expected)
      }

      it("should return false if the thread start request was not removed") {
        val expected = false

        val actual = threadStartManager.removeThreadStartRequest(TestId)
        actual should be (expected)
      }
    }
  }
}
