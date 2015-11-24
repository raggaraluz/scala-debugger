package org.senkbeil.debugger.api.lowlevel.vm

import java.util.concurrent.atomic.AtomicInteger

import com.sun.jdi.VirtualMachine
import com.sun.jdi.request.{EventRequest, EventRequestManager, VMDeathRequest}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.requests.{JDIRequestArgument, JDIRequestProcessor}

import scala.util.{Failure, Success}

class VMDeathManagerSpec extends FunSpec with Matchers with MockFactory
  with OneInstancePerTest with org.scalamock.matchers.Matchers
{
  private val TestId = java.util.UUID.randomUUID().toString
  private val mockEventRequestManager = mock[EventRequestManager]

  private val vmDeathManager = new VMDeathManager(mockEventRequestManager) {
    override protected def newRequestId(): String = TestId
  }

  describe("VMDeathManager") {
    describe("#vmDeathRequestList") {
      it("should contain all vm death requests in the form of id -> request stored in the manager") {
        val vmDeathRequests = Seq(
          java.util.UUID.randomUUID().toString,
          java.util.UUID.randomUUID().toString
        )

        // Create a VMDeathManager whose generated request ids match the list
        // above
        val vmDeathManager = new VMDeathManager(mockEventRequestManager) {
          private val counter = new AtomicInteger(0)
          override protected def newRequestId(): String = {
            vmDeathRequests(counter.getAndIncrement % vmDeathRequests.length)
          }
        }

        vmDeathRequests.foreach { case id =>
          (mockEventRequestManager.createVMDeathRequest _).expects()
            .returning(stub[VMDeathRequest]).once()
          vmDeathManager.createVMDeathRequest()
        }

        vmDeathManager.vmDeathRequestList should
          contain theSameElementsAs (vmDeathRequests)
      }
    }

    describe("#createVMDeathRequest") {
      it("should create the vm death request and return Success(id)") {
        val expected = Success(TestId)

        val mockVMDeathRequest = mock[VMDeathRequest]
        (mockEventRequestManager.createVMDeathRequest _).expects()
          .returning(mockVMDeathRequest).once()

        // Should set enabled to true by default, and
        // set the suspend policy to vm level by default
        (mockVMDeathRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockVMDeathRequest.setEnabled _).expects(true).once()

        val actual = vmDeathManager.createVMDeathRequest()
        actual should be (expected)
      }

      it("should return the exception if unable to create the request") {
        val expected = Failure(new Throwable)

        (mockEventRequestManager.createVMDeathRequest _).expects()
          .throwing(expected.failed.get).once()

        val actual = vmDeathManager.createVMDeathRequest()
        actual should be (expected)
      }
    }

    describe("#createVMDeathRequestWithId") {
      it("should create the vm death request using the provided id") {
        val expected = Success(java.util.UUID.randomUUID().toString)

        val mockVMDeathRequest = mock[VMDeathRequest]
        (mockEventRequestManager.createVMDeathRequest _).expects()
          .returning(mockVMDeathRequest).once()

        // Should set enabled to true by default, and
        // set the suspend policy to vm level by default
        (mockVMDeathRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockVMDeathRequest.setEnabled _).expects(true).once()

        val actual = vmDeathManager.createVMDeathRequestWithId(expected.get)
        actual should be (expected)
      }
    }

    describe("#hasVMDeathRequest") {
      it("should return true if it exists") {
        val expected = true

        (mockEventRequestManager.createVMDeathRequest _).expects()
          .returning(stub[VMDeathRequest]).once()

        val id = vmDeathManager.createVMDeathRequest().get

        val actual = vmDeathManager.hasVMDeathRequest(id)
        actual should be (expected)
      }

      it("should return false if it does not exist") {
        val expected = false

        val actual = vmDeathManager.hasVMDeathRequest(TestId)
        actual should be (expected)
      }
    }

    describe("#getVMDeathRequest") {
      it("should return Some(VMDeathRequest) if found") {
        val expected = stub[VMDeathRequest]

        (mockEventRequestManager.createVMDeathRequest _).expects()
          .returning(expected).once()

        val id = vmDeathManager.createVMDeathRequest().get

        val actual = vmDeathManager.getVMDeathRequest(id)
        actual should be (Some(expected))
      }

      it("should return None if not found") {
        val expected = None

        val actual = vmDeathManager.getVMDeathRequest(TestId)
        actual should be (expected)
      }
    }

    describe("#getVMDeathArguments") {
      it("should return Some(Seq(input args)) if found") {
        val expected = Seq(mock[JDIRequestArgument], mock[JDIRequestArgument])
        expected.foreach(a => {
          val mockRequestProcessor = mock[JDIRequestProcessor]
          (mockRequestProcessor.process _).expects(*)
            .onCall((er: EventRequest) => er).once()
          (a.toProcessor _).expects().returning(mockRequestProcessor).once()
        })

        (mockEventRequestManager.createVMDeathRequest _).expects()
          .returning(stub[VMDeathRequest]).once()

        val id = vmDeathManager.createVMDeathRequest(expected: _*).get

        val actual = vmDeathManager.getVMDeathRequestArguments(id)
        actual should be (Some(expected))
      }

      it("should return None if not found") {
        val expected = None

        val actual = vmDeathManager.getVMDeathRequestArguments(TestId)
        actual should be (expected)
      }
    }

    describe("#removeVMDeathRequest") {
      it("should return true if the vm death request was removed") {
        val expected = true
        val stubRequest = stub[VMDeathRequest]

        (mockEventRequestManager.createVMDeathRequest _).expects()
          .returning(stubRequest).once()

        val id = vmDeathManager.createVMDeathRequest().get

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubRequest).once()

        val actual = vmDeathManager.removeVMDeathRequest(id)
        actual should be (expected)
      }

      it("should return false if the vm death request was not removed") {
        val expected = false

        val actual = vmDeathManager.removeVMDeathRequest(TestId)
        actual should be (expected)
      }
    }
  }
}
