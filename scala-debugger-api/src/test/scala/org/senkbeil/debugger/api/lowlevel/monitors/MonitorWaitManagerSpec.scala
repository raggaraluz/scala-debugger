package org.senkbeil.debugger.api.lowlevel.monitors

import java.util.concurrent.atomic.AtomicInteger

import com.sun.jdi.request.{EventRequest, EventRequestManager, MonitorWaitRequest}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.requests.{JDIRequestArgument, JDIRequestProcessor}

import scala.util.{Failure, Success}

class MonitorWaitManagerSpec extends FunSpec with Matchers with MockFactory
  with OneInstancePerTest with org.scalamock.matchers.Matchers
{
  private val TestId = java.util.UUID.randomUUID().toString
  private val mockEventRequestManager = mock[EventRequestManager]

  private val monitorWaitManager = new MonitorWaitManager(mockEventRequestManager) {
    override protected def newRequestId(): String = TestId
  }

  describe("MonitorWaitManager") {
    describe("#monitorWaitRequestList") {
      it("should contain all monitor wait requests in the form of id -> request stored in the manager") {
        val monitorWaitRequests = Seq(
          java.util.UUID.randomUUID().toString,
          java.util.UUID.randomUUID().toString
        )

        // Create a MonitorWaitManager whose generated request ids match the list
        // above
        val monitorWaitManager = new MonitorWaitManager(mockEventRequestManager) {
          private val counter = new AtomicInteger(0)
          override protected def newRequestId(): String = {
            monitorWaitRequests(counter.getAndIncrement % monitorWaitRequests.length)
          }
        }

        monitorWaitRequests.foreach { case id =>
          (mockEventRequestManager.createMonitorWaitRequest _).expects()
            .returning(stub[MonitorWaitRequest]).once()
          monitorWaitManager.createMonitorWaitRequest()
        }

        monitorWaitManager.monitorWaitRequestList should
          contain theSameElementsAs (monitorWaitRequests)
      }
    }

    describe("#createMonitorWaitRequest") {
      it("should create the monitor wait request and return Success(id)") {
        val expected = Success(TestId)

        val mockMonitorWaitRequest = mock[MonitorWaitRequest]
        (mockEventRequestManager.createMonitorWaitRequest _).expects()
          .returning(mockMonitorWaitRequest).once()

        // Should set enabled to true by default, and
        // set the suspend policy to vm level by default
        (mockMonitorWaitRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockMonitorWaitRequest.setEnabled _).expects(true).once()

        val actual = monitorWaitManager.createMonitorWaitRequest()
        actual should be (expected)
      }

      it("should return the exception if unable to create the request") {
        val expected = Failure(new Throwable)

        (mockEventRequestManager.createMonitorWaitRequest _).expects()
          .throwing(expected.failed.get).once()

        val actual = monitorWaitManager.createMonitorWaitRequest()
        actual should be (expected)
      }
    }

    describe("#hasMonitorWaitRequest") {
      it("should return true if it exists") {
        val expected = true

        (mockEventRequestManager.createMonitorWaitRequest _).expects()
          .returning(stub[MonitorWaitRequest]).once()

        val id = monitorWaitManager.createMonitorWaitRequest().get

        val actual = monitorWaitManager.hasMonitorWaitRequest(id)
        actual should be (expected)
      }

      it("should return false if it does not exist") {
        val expected = false

        val actual = monitorWaitManager.hasMonitorWaitRequest(TestId)
        actual should be (expected)
      }
    }

    describe("#getMonitorWaitRequest") {
      it("should return Some(MonitorWaitRequest) if found") {
        val expected = stub[MonitorWaitRequest]

        (mockEventRequestManager.createMonitorWaitRequest _).expects()
          .returning(expected).once()

        val id = monitorWaitManager.createMonitorWaitRequest().get

        val actual = monitorWaitManager.getMonitorWaitRequest(id)
        actual should be (Some(expected))
      }

      it("should return None if not found") {
        val expected = None

        val actual = monitorWaitManager.getMonitorWaitRequest(TestId)
        actual should be (expected)
      }
    }

    describe("#getMonitorWaitArguments") {
      it("should return Some(Seq(input args)) if found") {
        val expected = Seq(mock[JDIRequestArgument], mock[JDIRequestArgument])
        expected.foreach(a => {
          val mockRequestProcessor = mock[JDIRequestProcessor]
          (mockRequestProcessor.process _).expects(*)
            .onCall((er: EventRequest) => er).once()
          (a.toProcessor _).expects().returning(mockRequestProcessor).once()
        })

        (mockEventRequestManager.createMonitorWaitRequest _).expects()
          .returning(stub[MonitorWaitRequest]).once()

        val id = monitorWaitManager.createMonitorWaitRequest(expected: _*).get

        val actual = monitorWaitManager.getMonitorWaitRequestArguments(id)
        actual should be (Some(expected))
      }

      it("should return None if not found") {
        val expected = None

        val actual = monitorWaitManager.getMonitorWaitRequestArguments(TestId)
        actual should be (expected)
      }
    }

    describe("#removeMonitorWaitRequest") {
      it("should return true if the monitor wait request was removed") {
        val expected = true
        val stubRequest = stub[MonitorWaitRequest]

        (mockEventRequestManager.createMonitorWaitRequest _).expects()
          .returning(stubRequest).once()

        val id = monitorWaitManager.createMonitorWaitRequest().get

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubRequest).once()

        val actual = monitorWaitManager.removeMonitorWaitRequest(id)
        actual should be (expected)
      }

      it("should return false if the monitor wait request was not removed") {
        val expected = false

        val actual = monitorWaitManager.removeMonitorWaitRequest(TestId)
        actual should be (expected)
      }
    }
  }
}
