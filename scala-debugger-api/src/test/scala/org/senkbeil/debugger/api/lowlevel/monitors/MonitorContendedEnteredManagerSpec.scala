package org.senkbeil.debugger.api.lowlevel.monitors

import java.util.concurrent.atomic.AtomicInteger

import com.sun.jdi.request.{MonitorContendedEnteredRequest, EventRequest, EventRequestManager}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.requests.{JDIRequestArgument, JDIRequestProcessor}

import scala.util.{Failure, Success}

class MonitorContendedEnteredManagerSpec extends FunSpec with Matchers with MockFactory
  with OneInstancePerTest with org.scalamock.matchers.Matchers
{
  private val TestId = java.util.UUID.randomUUID().toString
  private val mockEventRequestManager = mock[EventRequestManager]

  private val monitorContendedEnteredManager = new MonitorContendedEnteredManager(mockEventRequestManager) {
    override protected def newRequestId(): String = TestId
  }

  describe("MonitorContendedEnteredManager") {
    describe("#monitorContendedEnteredRequestList") {
      it("should contain all monitor contended entered requests in the form of id -> request stored in the manager") {
        val monitorContendedEnteredRequests = Seq(
          java.util.UUID.randomUUID().toString,
          java.util.UUID.randomUUID().toString
        )

        // Create a MonitorContendedEnteredManager whose generated request ids match the list
        // above
        val monitorContendedEnteredManager = new MonitorContendedEnteredManager(mockEventRequestManager) {
          private val counter = new AtomicInteger(0)
          override protected def newRequestId(): String = {
            monitorContendedEnteredRequests(counter.getAndIncrement % monitorContendedEnteredRequests.length)
          }
        }

        monitorContendedEnteredRequests.foreach { case id =>
          (mockEventRequestManager.createMonitorContendedEnteredRequest _).expects()
            .returning(stub[MonitorContendedEnteredRequest]).once()
          monitorContendedEnteredManager.createMonitorContendedEnteredRequest()
        }

        monitorContendedEnteredManager.monitorContendedEnteredRequestList should
          contain theSameElementsAs (monitorContendedEnteredRequests)
      }
    }

    describe("#createMonitorContendedEnteredRequestWithId") {
      it("should create the monitor contended entered request using the provided id") {
        val expected = Success(java.util.UUID.randomUUID().toString)

        val mockMonitorContendedEnteredRequest = mock[MonitorContendedEnteredRequest]
        (mockEventRequestManager.createMonitorContendedEnteredRequest _).expects()
          .returning(mockMonitorContendedEnteredRequest).once()

        // Should set enabled to true by default, and
        // set the suspend policy to vm level by default
        (mockMonitorContendedEnteredRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockMonitorContendedEnteredRequest.setEnabled _).expects(true).once()

        val actual = monitorContendedEnteredManager.createMonitorContendedEnteredRequestWithId(expected.get)
        actual should be(expected)
      }
    }

    describe("#createMonitorContendedEnteredRequest") {
      it("should create the monitor contended entered request and return Success(id)") {
        val expected = Success(TestId)

        val mockMonitorContendedEnteredRequest = mock[MonitorContendedEnteredRequest]
        (mockEventRequestManager.createMonitorContendedEnteredRequest _).expects()
          .returning(mockMonitorContendedEnteredRequest).once()

        // Should set enabled to true by default, and
        // set the suspend policy to vm level by default
        (mockMonitorContendedEnteredRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockMonitorContendedEnteredRequest.setEnabled _).expects(true).once()

        val actual = monitorContendedEnteredManager.createMonitorContendedEnteredRequest()
        actual should be (expected)
      }

      it("should return the exception if unable to create the request") {
        val expected = Failure(new Throwable)

        (mockEventRequestManager.createMonitorContendedEnteredRequest _).expects()
          .throwing(expected.failed.get).once()

        val actual = monitorContendedEnteredManager.createMonitorContendedEnteredRequest()
        actual should be (expected)
      }
    }

    describe("#hasMonitorContendedEnteredRequest") {
      it("should return true if it exists") {
        val expected = true

        (mockEventRequestManager.createMonitorContendedEnteredRequest _).expects()
          .returning(stub[MonitorContendedEnteredRequest]).once()

        val id = monitorContendedEnteredManager.createMonitorContendedEnteredRequest().get

        val actual = monitorContendedEnteredManager.hasMonitorContendedEnteredRequest(id)
        actual should be (expected)
      }

      it("should return false if it does not exist") {
        val expected = false

        val actual = monitorContendedEnteredManager.hasMonitorContendedEnteredRequest(TestId)
        actual should be (expected)
      }
    }

    describe("#getMonitorContendedEnteredRequest") {
      it("should return Some(MonitorContendedEnteredRequest) if found") {
        val expected = stub[MonitorContendedEnteredRequest]

        (mockEventRequestManager.createMonitorContendedEnteredRequest _).expects()
          .returning(expected).once()

        val id = monitorContendedEnteredManager.createMonitorContendedEnteredRequest().get

        val actual = monitorContendedEnteredManager.getMonitorContendedEnteredRequest(id)
        actual should be (Some(expected))
      }

      it("should return None if not found") {
        val expected = None

        val actual = monitorContendedEnteredManager.getMonitorContendedEnteredRequest(TestId)
        actual should be (expected)
      }
    }

    describe("#getMonitorContendedEnteredArguments") {
      it("should return Some(Seq(input args)) if found") {
        val expected = Seq(mock[JDIRequestArgument], mock[JDIRequestArgument])
        expected.foreach(a => {
          val mockRequestProcessor = mock[JDIRequestProcessor]
          (mockRequestProcessor.process _).expects(*)
            .onCall((er: EventRequest) => er).once()
          (a.toProcessor _).expects().returning(mockRequestProcessor).once()
        })

        (mockEventRequestManager.createMonitorContendedEnteredRequest _).expects()
          .returning(stub[MonitorContendedEnteredRequest]).once()

        val id = monitorContendedEnteredManager.createMonitorContendedEnteredRequest(expected: _*).get

        val actual = monitorContendedEnteredManager.getMonitorContendedEnteredRequestArguments(id)
        actual should be (Some(expected))
      }

      it("should return None if not found") {
        val expected = None

        val actual = monitorContendedEnteredManager.getMonitorContendedEnteredRequestArguments(TestId)
        actual should be (expected)
      }
    }

    describe("#removeMonitorContendedEnteredRequest") {
      it("should return true if the monitor contended entered request was removed") {
        val expected = true
        val stubRequest = stub[MonitorContendedEnteredRequest]

        (mockEventRequestManager.createMonitorContendedEnteredRequest _).expects()
          .returning(stubRequest).once()

        val id = monitorContendedEnteredManager.createMonitorContendedEnteredRequest().get

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubRequest).once()

        val actual = monitorContendedEnteredManager.removeMonitorContendedEnteredRequest(id)
        actual should be (expected)
      }

      it("should return false if the monitor contended entered request was not removed") {
        val expected = false

        val actual = monitorContendedEnteredManager.removeMonitorContendedEnteredRequest(TestId)
        actual should be (expected)
      }
    }
  }
}
