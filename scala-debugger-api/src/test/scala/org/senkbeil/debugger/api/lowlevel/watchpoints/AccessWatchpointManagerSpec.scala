package org.senkbeil.debugger.api.lowlevel.watchpoints

import com.sun.jdi.{ReferenceType, VirtualMachine, Field}
import com.sun.jdi.request.{EventRequest, EventRequestManager, AccessWatchpointRequest}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.classes.ClassManager
import test.JDIMockHelpers
import scala.collection.JavaConverters._

import scala.util.{Failure, Success}

class AccessWatchpointManagerSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockField = mock[Field]
  private val mockEventRequestManager = mock[EventRequestManager]

  // NOTE: Needed until https://github.com/paulbutcher/ScalaMock/issues/56
  class ZeroArgClassManager
    extends ClassManager(stub[VirtualMachine], loadClasses = false)
  private val mockClassManager = mock[ZeroArgClassManager]

  private val accessWatchpointManager = new AccessWatchpointManager(
    mockEventRequestManager,
    mockClassManager
  ) {
    override protected def newRequestId(): String = TestRequestId
  }

  describe("AccessWatchpointManager") {
    describe("#accessWatchpointRequestListById") {
      it("should contain all access watchpoint requests in the form of field stored in the manager") {
        val requestIds = Seq(TestRequestId, TestRequestId + 1, TestRequestId + 2)

        requestIds.foreach { case requestId =>
          val mockField = mock[Field]
          (mockEventRequestManager.createAccessWatchpointRequest _)
            .expects(mockField)
            .returning(stub[AccessWatchpointRequest]).once()
          accessWatchpointManager.createAccessWatchpointRequestWithId(
            requestId,
            mockField
          )
        }

        accessWatchpointManager.accessWatchpointRequestListById should
          contain theSameElementsAs (requestIds)
      }
    }

    describe("#accessWatchpointRequestList") {
      it("should contain all access watchpoint requests in the form of field stored in the manager") {
        val mockFields = Seq(
          mock[Field],
          mock[Field],
          mock[Field]
        )

        // NOTE: Must create a new accessWatchpoint manager that does NOT override the
        //       request id to always be the same since we do not allow
        //       duplicates of the test id when storing it
        val accessWatchpointManager = new AccessWatchpointManager(
          mockEventRequestManager,
          mockClassManager
        )

        mockFields.foreach { case f =>
          (mockEventRequestManager.createAccessWatchpointRequest _).expects(f)
            .returning(stub[AccessWatchpointRequest]).once()
          accessWatchpointManager.createAccessWatchpointRequest(f)
        }

        accessWatchpointManager.accessWatchpointRequestList should
          contain theSameElementsAs (mockFields)
      }
    }

    describe("#createAccessWatchpointRequestByNameWithId") {
      it("should create the access watchpoint request using the provided id") {
        val expected = Success(java.util.UUID.randomUUID().toString)
        val testClassName = "full.class.name"
        val testFieldName = "fieldName"

        val stubField = createFieldStub(testFieldName)
        val mockReferenceType = mock[ReferenceType]
        (mockClassManager.allClasses _).expects()
          .returning(Seq(mockReferenceType)).once()
        (mockReferenceType.name _).expects().returning(testClassName).once()
        (mockReferenceType.allFields _).expects()
          .returning(Seq(stubField).asJava).once()

        val mockAccessWatchpointRequest = mock[AccessWatchpointRequest]
        (mockEventRequestManager.createAccessWatchpointRequest _)
          .expects(stubField)
          .returning(mockAccessWatchpointRequest).once()

        (mockAccessWatchpointRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockAccessWatchpointRequest.setEnabled _).expects(true).once()

        val actual = accessWatchpointManager.createAccessWatchpointRequestByNameWithId(
          expected.get,
          testClassName,
          testFieldName
        )
        actual should be(expected)
      }
    }

    describe("#createAccessWatchpointRequestByName") {
      it("should create the access watchpoint request and return Success(id)") {
        val expected = Success(TestRequestId)
        val testClassName = "full.class.name"
        val testFieldName = "fieldName"

        val stubField = createFieldStub(testFieldName)
        val mockReferenceType = mock[ReferenceType]
        (mockClassManager.allClasses _).expects()
          .returning(Seq(mockReferenceType)).once()
        (mockReferenceType.name _).expects().returning(testClassName).once()
        (mockReferenceType.allFields _).expects()
          .returning(Seq(stubField).asJava).once()

        val mockAccessWatchpointRequest = mock[AccessWatchpointRequest]
        (mockEventRequestManager.createAccessWatchpointRequest _)
          .expects(stubField)
          .returning(mockAccessWatchpointRequest).once()

        (mockAccessWatchpointRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockAccessWatchpointRequest.setEnabled _).expects(true).once()

        val actual = accessWatchpointManager.createAccessWatchpointRequestByName(
          testClassName,
          testFieldName
        )
        actual should be (expected)
      }

      it("should return the exception if failed to create the access watchpoint request") {
        val expected = Failure(new Throwable)
        val testClassName = "full.class.name"
        val testFieldName = "fieldName"

        val stubField = createFieldStub(testFieldName)
        val mockReferenceType = mock[ReferenceType]
        (mockClassManager.allClasses _).expects()
          .returning(Seq(mockReferenceType)).once()
        (mockReferenceType.name _).expects().returning(testClassName).once()
        (mockReferenceType.allFields _).expects()
          .returning(Seq(stubField).asJava).once()

        (mockEventRequestManager.createAccessWatchpointRequest _)
          .expects(stubField)
          .throwing(expected.failed.get).once()

        val actual = accessWatchpointManager.createAccessWatchpointRequestByName(
          testClassName,
          testFieldName
        )
        actual should be (expected)
      }

      it("should return a failure if the class of the field was not found") {
        val testClassName = "full.class.name"
        val testFieldName = "fieldName"
        val expected = Failure(NoFieldFound(testClassName, testFieldName))

        val mockReferenceType = mock[ReferenceType]
        (mockClassManager.allClasses _).expects()
          .returning(Seq(mockReferenceType)).once()

        // Provide reference types with different names so there is no match
        (mockReferenceType.name _).expects().returning(testClassName + 1).once()

        val actual = accessWatchpointManager.createAccessWatchpointRequestByName(
          testClassName,
          testFieldName
        )
        actual should be (expected)
      }

      it("should return a failure if the field with the specified name was not found") {
        val testClassName = "full.class.name"
        val testFieldName = "fieldName"
        val expected = Failure(NoFieldFound(testClassName, testFieldName))

        val mockReferenceType = mock[ReferenceType]
        (mockClassManager.allClasses _).expects()
          .returning(Seq(mockReferenceType)).once()
        (mockReferenceType.name _).expects().returning(testClassName).once()

        // Provide fields with different names so there is no match
        val stubField = createFieldStub(testFieldName + 1)
        (mockReferenceType.allFields _).expects()
          .returning(Seq(stubField).asJava).once()

        val actual = accessWatchpointManager.createAccessWatchpointRequestByName(
          testClassName,
          testFieldName
        )
        actual should be (expected)
      }
    }

    describe("#createAccessWatchpointRequestWithId") {
      it("should create the access watchpoint request using the provided id") {
        val expected = Success(java.util.UUID.randomUUID().toString)

        val mockAccessWatchpointRequest = mock[AccessWatchpointRequest]
        (mockEventRequestManager.createAccessWatchpointRequest _)
          .expects(mockField)
          .returning(mockAccessWatchpointRequest).once()

        (mockAccessWatchpointRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockAccessWatchpointRequest.setEnabled _).expects(true).once()

        val actual = accessWatchpointManager.createAccessWatchpointRequestWithId(
          expected.get,
          mockField
        )
        actual should be(expected)
      }
    }

    describe("#createAccessWatchpointRequest") {
      it("should create the access watchpoint request and return Success(id)") {
        val expected = Success(TestRequestId)

        val mockAccessWatchpointRequest = mock[AccessWatchpointRequest]
        (mockEventRequestManager.createAccessWatchpointRequest _)
          .expects(mockField)
          .returning(mockAccessWatchpointRequest).once()

        (mockAccessWatchpointRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockAccessWatchpointRequest.setEnabled _).expects(true).once()

        val actual = accessWatchpointManager.createAccessWatchpointRequest(mockField)
        actual should be (expected)
      }

      it("should return the exception if failed to create the access watchpoint request") {
        val expected = Failure(new Throwable)

        (mockEventRequestManager.createAccessWatchpointRequest _)
          .expects(mockField)
          .throwing(expected.failed.get).once()

        val actual = accessWatchpointManager.createAccessWatchpointRequest(mockField)
        actual should be (expected)
      }
    }

    describe("#hasAccessWatchpointRequestWithId") {
      it("should return true if it exists") {
        val expected = true

        (mockEventRequestManager.createAccessWatchpointRequest _)
          .expects(mockField)
          .returning(stub[AccessWatchpointRequest]).once()

        accessWatchpointManager.createAccessWatchpointRequestWithId(
          TestRequestId,
          mockField
        )

        val actual = accessWatchpointManager.hasAccessWatchpointRequestWithId(TestRequestId)
        actual should be (expected)
      }

      it("should return false if it does not exist") {
        val expected = false

        val actual = accessWatchpointManager.hasAccessWatchpointRequestWithId(TestRequestId)
        actual should be (expected)
      }
    }

    describe("#hasAccessWatchpointRequest") {
      it("should return true if it exists") {
        val expected = true

        val testSize = 0
        val testDepth = 1

        (mockEventRequestManager.createAccessWatchpointRequest _)
          .expects(mockField)
          .returning(stub[AccessWatchpointRequest]).once()

        accessWatchpointManager.createAccessWatchpointRequest(mockField)

        val actual = accessWatchpointManager.hasAccessWatchpointRequest(mockField)
        actual should be (expected)
      }

      it("should return false if it does not exist") {
        val expected = false

        val actual = accessWatchpointManager.hasAccessWatchpointRequest(mockField)
        actual should be (expected)
      }
    }

    describe("#getAccessWatchpointRequestWithId") {
      it("should return Some(AccessWatchpointRequest) if found") {
        val expected = Some(stub[AccessWatchpointRequest])

        (mockEventRequestManager.createAccessWatchpointRequest _)
          .expects(mockField)
          .returning(expected.get).once()

        accessWatchpointManager.createAccessWatchpointRequestWithId(
          TestRequestId,
          mockField
        )

        val actual = accessWatchpointManager.getAccessWatchpointRequestWithId(TestRequestId)
        actual should be (expected)
      }

      it("should return None if not found") {
        val expected = None

        val actual = accessWatchpointManager.getAccessWatchpointRequestWithId(TestRequestId)
        actual should be (expected)
      }
    }

    describe("#getAccessWatchpointRequest") {
      it("should return Some(Seq(AccessWatchpointRequest)) if found") {
        val expected = Seq(stub[AccessWatchpointRequest])

        (mockEventRequestManager.createAccessWatchpointRequest _)
          .expects(mockField)
          .returning(expected.head).once()

        accessWatchpointManager.createAccessWatchpointRequest(
          mockField
        )

        val actual = accessWatchpointManager.getAccessWatchpointRequest(mockField).get
        actual should contain theSameElementsAs (expected)
      }

      it("should return None if not found") {
        val expected = None

        val actual = accessWatchpointManager.getAccessWatchpointRequest(mockField)
        actual should be (expected)
      }
    }

    describe("#removeAccessWatchpointRequestWithId") {
      it("should return true if the access watchpoint request was removed") {
        val expected = true
        val stubRequest = stub[AccessWatchpointRequest]

        (mockEventRequestManager.createAccessWatchpointRequest _)
          .expects(mockField)
          .returning(stubRequest).once()

        accessWatchpointManager.createAccessWatchpointRequestWithId(
          TestRequestId,
          mockField
        )

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubRequest).once()

        val actual = accessWatchpointManager.removeAccessWatchpointRequestWithId(TestRequestId)
        actual should be (expected)
      }

      it("should return false if the access watchpoint request was not removed") {
        val expected = false

        val actual = accessWatchpointManager.removeAccessWatchpointRequestWithId(TestRequestId)
        actual should be (expected)
      }
    }

    describe("#removeAccessWatchpointRequest") {
      it("should return true if the access watchpoint request was removed") {
        val expected = true
        val stubRequest = stub[AccessWatchpointRequest]

        (mockEventRequestManager.createAccessWatchpointRequest _)
          .expects(mockField)
          .returning(stubRequest).once()

        accessWatchpointManager.createAccessWatchpointRequest(mockField)

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubRequest).once()

        val actual = accessWatchpointManager.removeAccessWatchpointRequest(mockField)
        actual should be (expected)
      }

      it("should return false if the access watchpoint request was not removed") {
        val expected = false

        val actual = accessWatchpointManager.removeAccessWatchpointRequest(mockField)
        actual should be (expected)
      }
    }
  }
}
