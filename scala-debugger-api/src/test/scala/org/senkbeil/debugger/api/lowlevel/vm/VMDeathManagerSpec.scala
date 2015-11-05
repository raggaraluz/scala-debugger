package org.senkbeil.debugger.api.lowlevel.vm

import java.util.concurrent.atomic.AtomicInteger

import com.sun.jdi.VirtualMachine
import com.sun.jdi.request.{EventRequest, EventRequestManager, VMDeathRequest}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}

import scala.util.{Failure, Success}

class VMDeathManagerSpec extends FunSpec with Matchers with MockFactory
  with OneInstancePerTest with org.scalamock.matchers.Matchers
{
  private val TestId = java.util.UUID.randomUUID().toString
  private val mockEventRequestManager = mock[EventRequestManager]

  private val mockVirtualMachine = mock[VirtualMachine]
  (mockVirtualMachine.eventRequestManager _).expects()
    .returning(mockEventRequestManager).once()

  private val vmDeathManager = new VMDeathManager(mockVirtualMachine) {
    override protected def newRequestId(): String = TestId
  }

  describe("VMDeathManager") {
    describe("#vmDeathList") {
      it("should contain all vm death requests in the form of id -> request stored in the manager") {
        val vmDeathRequests = Seq(
          java.util.UUID.randomUUID().toString,
          java.util.UUID.randomUUID().toString
        )

        // Create a VMDeathManager whose generated request ids match the list
        // above
        (mockVirtualMachine.eventRequestManager _).expects()
          .returning(mockEventRequestManager).once()
        val vmDeathManager = new VMDeathManager(mockVirtualMachine) {
          private val counter = new AtomicInteger(0)
          override protected def newRequestId(): String = {
            vmDeathRequests(counter.getAndIncrement % vmDeathRequests.length)
          }
        }

        vmDeathRequests.foreach { case id =>
          (mockEventRequestManager.createVMDeathRequest _).expects()
            .returning(stub[VMDeathRequest]).once()
          vmDeathManager.setVMDeath()
        }

        vmDeathManager.vmDeathList should
          contain theSameElementsAs (vmDeathRequests)
      }
    }

    describe("#setVMDeath") {
      it("should create the vm death request and return Success(id)") {
        val expected = Success(TestId)

        val mockVMDeathRequest = mock[VMDeathRequest]
        (mockEventRequestManager.createVMDeathRequest _).expects()
          .returning(mockVMDeathRequest).once()

        // Should set enabled to true by default, and
        // set the suspend policy to thread level by default
        (mockVMDeathRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockVMDeathRequest.setEnabled _).expects(true).once()

        val actual = vmDeathManager.setVMDeath()
        actual should be (expected)
      }

      it("should return the exception if unable to create the request") {
        val expected = Failure(new Throwable)

        (mockEventRequestManager.createVMDeathRequest _).expects()
          .throwing(expected.failed.get).once()

        val actual = vmDeathManager.setVMDeath()
        actual should be (expected)
      }
    }

    describe("#hasVMDeath") {
      it("should return true if it exists") {
        val expected = true

        (mockEventRequestManager.createVMDeathRequest _).expects()
          .returning(stub[VMDeathRequest]).once()

        val id = vmDeathManager.setVMDeath().get

        val actual = vmDeathManager.hasVMDeath(id)
        actual should be (expected)
      }

      it("should return false if it does not exist") {
        val expected = false

        val actual = vmDeathManager.hasVMDeath(TestId)
        actual should be (expected)
      }
    }

    describe("#getVMDeath") {
      it("should return Some(VMDeathRequest) if found") {
        val expected = stub[VMDeathRequest]

        val testClassName = "some class name"
        val testMethodName = "some method name"

        (mockEventRequestManager.createVMDeathRequest _).expects()
          .returning(expected).once()

        val id = vmDeathManager.setVMDeath().get

        val actual = vmDeathManager.getVMDeath(id)
        actual should be (Some(expected))
      }

      it("should return None if not found") {
        val expected = None

        val actual = vmDeathManager.getVMDeath(TestId)
        actual should be (expected)
      }
    }

    describe("#removeVMDeath") {
      it("should return true if the vm death request was removed") {
        val expected = true
        val stubRequest = stub[VMDeathRequest]

        val testClassName = "some class name"
        val testMethodName = "some method name"

        (mockEventRequestManager.createVMDeathRequest _).expects()
          .returning(stubRequest).once()

        val id = vmDeathManager.setVMDeath().get

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubRequest).once()

        val actual = vmDeathManager.removeVMDeath(id)
        actual should be (expected)
      }

      it("should return false if the vm death request was not removed") {
        val expected = false

        val actual = vmDeathManager.removeVMDeath(TestId)
        actual should be (expected)
      }
    }
  }
}
