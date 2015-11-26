package org.senkbeil.debugger.api.lowlevel.monitors

import java.util.concurrent.atomic.AtomicInteger

import com.sun.jdi.request.{EventRequest, EventRequestManager, MonitorWaitedRequest}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.requests.{JDIRequestArgument, JDIRequestProcessor}

import scala.util.{Failure, Success}

class MonitorWaitedManagerSpec extends FunSpec with Matchers with MockFactory
  with OneInstancePerTest with org.scalamock.matchers.Matchers
{
  private val TestId = java.util.UUID.randomUUID().toString
  private val mockEventRequestManager = mock[EventRequestManager]

  private val monitorWaitedManager = new MonitorWaitedManager(mockEventRequestManager) {
    override protected def newRequestId(): String = TestId
  }

  describe("MonitorWaitedManager") {
    describe("#monitorWaitedRequestList") {
      it("should contain all monitor waited requests in the form of id -> request stored in the manager") {
        val monitorWaitedRequests = Seq(
          java.util.UUID.randomUUID().toString,
          java.util.UUID.randomUUID().toString
        )

        // Create a MonitorWaitedManager whose generated request ids match the list
        // above
        val monitorWaitedManager = new MonitorWaitedManager(mockEventRequestManager) {
          private val counter = new AtomicInteger(0)
          override protected def newRequestId(): String = {
            monitorWaitedRequests(counter.getAndIncrement % monitorWaitedRequests.length)
          }
        }

        monitorWaitedRequests.foreach { case id =>
          (mockEventRequestManager.createMonitorWaitedRequest _).expects()
            .returning(stub[MonitorWaitedRequest]).once()
          monitorWaitedManager.createMonitorWaitedRequest()
        }

        monitorWaitedManager.monitorWaitedRequestList should
          contain theSameElementsAs (monitorWaitedRequests)
      }
    }

    describe("#createMonitorWaitedRequestWithId") {
      it("should create the monitor waited request using the provided id") {
        val expected = Success(java.util.UUID.randomUUID().toString)

        val mockMonitorWaitedRequest = mock[MonitorWaitedRequest]
        (mockEventRequestManager.createMonitorWaitedRequest _).expects()
          .returning(mockMonitorWaitedRequest).once()

        // Should set enabled to true by default, and
        // set the suspend policy to vm level by default
        (mockMonitorWaitedRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockMonitorWaitedRequest.setEnabled _).expects(true).once()

        val actual =
          monitorWaitedManager.createMonitorWaitedRequestWithId(expected.get)
        actual should be(expected)
      }
    }

    describe("#createMonitorWaitedRequest") {
      it("should create the monitor waited request and return Success(id)") {
        val expected = Success(TestId)

        val mockMonitorWaitedRequest = mock[MonitorWaitedRequest]
        (mockEventRequestManager.createMonitorWaitedRequest _).expects()
          .returning(mockMonitorWaitedRequest).once()

        // Should set enabled to true by default, and
        // set the suspend policy to vm level by default
        (mockMonitorWaitedRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockMonitorWaitedRequest.setEnabled _).expects(true).once()

        val actual = monitorWaitedManager.createMonitorWaitedRequest()
        actual should be (expected)
      }

      it("should return the exception if unable to create the request") {
        val expected = Failure(new Throwable)

        (mockEventRequestManager.createMonitorWaitedRequest _).expects()
          .throwing(expected.failed.get).once()

        val actual = monitorWaitedManager.createMonitorWaitedRequest()
        actual should be (expected)
      }
    }

    describe("#hasMonitorWaitedRequest") {
      it("should return true if it exists") {
        val expected = true

        (mockEventRequestManager.createMonitorWaitedRequest _).expects()
          .returning(stub[MonitorWaitedRequest]).once()

        val id = monitorWaitedManager.createMonitorWaitedRequest().get

        val actual = monitorWaitedManager.hasMonitorWaitedRequest(id)
        actual should be (expected)
      }

      it("should return false if it does not exist") {
        val expected = false

        val actual = monitorWaitedManager.hasMonitorWaitedRequest(TestId)
        actual should be (expected)
      }
    }

    describe("#getMonitorWaitedRequest") {
      it("should return Some(MonitorWaitedRequest) if found") {
        val expected = stub[MonitorWaitedRequest]

        (mockEventRequestManager.createMonitorWaitedRequest _).expects()
          .returning(expected).once()

        val id = monitorWaitedManager.createMonitorWaitedRequest().get

        val actual = monitorWaitedManager.getMonitorWaitedRequest(id)
        actual should be (Some(expected))
      }

      it("should return None if not found") {
        val expected = None

        val actual = monitorWaitedManager.getMonitorWaitedRequest(TestId)
        actual should be (expected)
      }
    }

    describe("#getMonitorWaitedArguments") {
      it("should return Some(Seq(input args)) if found") {
        val expected = Seq(mock[JDIRequestArgument], mock[JDIRequestArgument])
        expected.foreach(a => {
          val mockRequestProcessor = mock[JDIRequestProcessor]
          (mockRequestProcessor.process _).expects(*)
            .onCall((er: EventRequest) => er).once()
          (a.toProcessor _).expects().returning(mockRequestProcessor).once()
        })

        (mockEventRequestManager.createMonitorWaitedRequest _).expects()
          .returning(stub[MonitorWaitedRequest]).once()

        val id = monitorWaitedManager.createMonitorWaitedRequest(expected: _*).get

        val actual = monitorWaitedManager.getMonitorWaitedRequestArguments(id)
        actual should be (Some(expected))
      }

      it("should return None if not found") {
        val expected = None

        val actual = monitorWaitedManager.getMonitorWaitedRequestArguments(TestId)
        actual should be (expected)
      }
    }

    describe("#removeMonitorWaitedRequest") {
      it("should return true if the monitor waited request was removed") {
        val expected = true
        val stubRequest = stub[MonitorWaitedRequest]

        (mockEventRequestManager.createMonitorWaitedRequest _).expects()
          .returning(stubRequest).once()

        val id = monitorWaitedManager.createMonitorWaitedRequest().get

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubRequest).once()

        val actual = monitorWaitedManager.removeMonitorWaitedRequest(id)
        actual should be (expected)
      }

      it("should return false if the monitor waited request was not removed") {
        val expected = false

        val actual = monitorWaitedManager.removeMonitorWaitedRequest(TestId)
        actual should be (expected)
      }
    }
  }
}
