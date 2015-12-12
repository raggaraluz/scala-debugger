package org.senkbeil.debugger.api.lowlevel.breakpoints

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.utils.{ActionInfo, PendingActionManager}
import test.{JDIMockHelpers, TestBreakpointManager}

import scala.util.{Failure, Success}

class PendingBreakpointSupportSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockBreakpointManager = mock[BreakpointManager]

  private class TestBreakpointInfoPendingActionManager
    extends PendingActionManager[BreakpointRequestInfo]
  private val mockPendingActionManager =
    mock[TestBreakpointInfoPendingActionManager]

  private val pendingBreakpointSupport = new TestBreakpointManager(
    mockBreakpointManager
  ) with PendingBreakpointSupport {
    override protected def newRequestId(): String = TestRequestId

    override protected val pendingActionManager: PendingActionManager[BreakpointRequestInfo] =
      mockPendingActionManager
  }

  describe("PendingBreakpointSupport") {
    describe("#processAllPendingBreakpointRequests") {
      it("should process all pending breakpoints") {
        val testFileName = "some/file/name"
        val testLineNumber = 1

        val expected = Seq(
          BreakpointRequestInfo(testFileName, testLineNumber),
          BreakpointRequestInfo(testFileName + 1, testLineNumber),
          BreakpointRequestInfo(testFileName, testLineNumber + 1)
        )

        // Create breakpoints to use for testing
        (mockBreakpointManager.createBreakpointRequestWithId _)
          .expects(*, *, *, *)
          .returning(Success(java.util.UUID.randomUUID().toString))
          .repeated(3).times()

        expected.foreach(b => pendingBreakpointSupport.createBreakpointRequest(
          b.fileName, b.lineNumber, b.extraArguments: _*
        ))

        (mockPendingActionManager.processAllActions _).expects()
          .returning(expected.map(b => ActionInfo("id", b, () => {}))).once()

        val actual = pendingBreakpointSupport.processAllPendingBreakpointRequests()
        actual should be (expected)
      }
    }

    describe("#processPendingBreakpointRequestsForFile") {
      it("should process pending breakpoints for the specified file") {
        val testFileName = "some/file/name"
        val testLineNumber = 1

        val expected = Seq(
          BreakpointRequestInfo(testFileName, testLineNumber, Nil)
        )

        // Create a breakpoint to use for testing
        (mockBreakpointManager.createBreakpointRequestWithId _)
          .expects(TestRequestId, testFileName, testLineNumber, Nil)
          .returning(Success(TestRequestId)).once()

        pendingBreakpointSupport.createBreakpointRequest(
          testFileName,
          testLineNumber
        )

        // Return our data that represents the expected file and line
        (mockPendingActionManager.processActions _).expects(*)
          .returning(Seq(ActionInfo("id", expected.head, () => {})))
          .once()

        val actual = pendingBreakpointSupport.processPendingBreakpointRequestsForFile(
          testFileName
        )

        actual should be (expected)
      }
    }

    describe("#pendingBreakpointRequestsForFile") {
      it("should return a collection of pending breakpoints") {
        val testFileName = "some/file/name"
        val testLineNumber = 1

        val expected = Seq(BreakpointRequestInfo(testFileName, testLineNumber))

        // Create a breakpoint to use for testing
        (mockBreakpointManager.createBreakpointRequestWithId _)
          .expects(TestRequestId, testFileName, testLineNumber, Nil)
          .returning(Success(TestRequestId)).once()

        pendingBreakpointSupport.createBreakpointRequest(
          testFileName,
          testLineNumber
        )

        // Return our data that represents the expected file and line
        (mockPendingActionManager.getPendingActionData _).expects(*)
          .returning(Seq(BreakpointRequestInfo(testFileName, testLineNumber, Nil)))
          .once()

        val actual =
          pendingBreakpointSupport.pendingBreakpointRequestsForFile(testFileName)

        actual should be (expected)
      }

      it("should be empty if there are no pending breakpoints") {
        val expected = Nil

        // No pending breakpoints
        (mockPendingActionManager.getPendingActionData _).expects(*)
          .returning(Nil).once()

        val actual = pendingBreakpointSupport.pendingBreakpointRequestsForFile("file")

        actual should be (expected)
      }
    }

    describe("#createBreakpointRequestWithId") {
      it("should return Success(id) if the breakpoint was created") {
        val testFileName = "some/file/name"
        val testLineNumber = 1

        val expected = Success(TestRequestId)

        // Create a breakpoint to use for testing
        (mockBreakpointManager.createBreakpointRequestWithId _)
          .expects(TestRequestId, testFileName, testLineNumber, Nil)
          .returning(expected).once()

        val actual = pendingBreakpointSupport.createBreakpointRequestWithId(
          TestRequestId,
          testFileName,
          testLineNumber
        )

        actual should be (expected)
      }

      it("should add a pending breakpoint if NoBreakpointLocationFound thrown") {
        val testFileName = "some/file/name"
        val testLineNumber = 1
        val error = NoBreakpointLocationFound(testFileName, testLineNumber)

        val expected = Success(TestRequestId)

        // Create a breakpoint to use for testing
        (mockBreakpointManager.createBreakpointRequestWithId _)
          .expects(TestRequestId, testFileName, testLineNumber, Nil)
          .returning(Failure(error)).once()

        // Pending breakpoint should be set
        (mockPendingActionManager.addPendingActionWithId _).expects(
          TestRequestId,
          BreakpointRequestInfo(testFileName, testLineNumber, Nil),
          * // Don't care about checking action
        ).returning(TestRequestId).once()

        val actual = pendingBreakpointSupport.createBreakpointRequestWithId(
          TestRequestId,
          testFileName,
          testLineNumber
        )

        actual should be (expected)
      }

      it("should return a failure if pending disabled and failed to create request") {
        val expected = Failure(new Throwable)
        val testFileName = "some/file/name"
        val testLineNumber = 1
        val extraArguments = Seq(stub[JDIRequestArgument])

        (mockBreakpointManager.createBreakpointRequestWithId _)
          .expects(*, *, *, *)
          .returning(expected).once()

        pendingBreakpointSupport.disablePendingSupport()
        val actual = pendingBreakpointSupport.createBreakpointRequestWithId(
          TestRequestId, testFileName, testLineNumber, extraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#createBreakpointRequest") {
      it("should return Success(id) if the breakpoint was created") {
        val testFileName = "some/file/name"
        val testLineNumber = 1

        val expected = Success(TestRequestId)

        (mockBreakpointManager.createBreakpointRequestWithId _)
          .expects(TestRequestId, testFileName, testLineNumber, Nil)
          .returning(expected).once()

        val actual = pendingBreakpointSupport.createBreakpointRequest(
          testFileName,
          testLineNumber
        )

        actual should be (expected)
      }

      it("should add a pending breakpoint if excpetion thrown") {
        val testFileName = "some/file/name"
        val testLineNumber = 1

        val expected = Success(TestRequestId)

        (mockBreakpointManager.createBreakpointRequestWithId _)
          .expects(TestRequestId, testFileName, testLineNumber, Nil)
          .returning(Failure(new Throwable)).once()

        // Pending breakpoint should be set
        (mockPendingActionManager.addPendingActionWithId _).expects(
          TestRequestId,
          BreakpointRequestInfo(testFileName, testLineNumber, Nil),
          * // Don't care about checking action
        ).returning(TestRequestId).once()

        val actual = pendingBreakpointSupport.createBreakpointRequest(
          testFileName,
          testLineNumber
        )

        actual should be (expected)
      }

      it("should return a failure if pending disabled and failed to create request") {
        val expected = Failure(new Throwable)
        val testFileName = "some/file/name"
        val testLineNumber = 1
        val extraArguments = Seq(stub[JDIRequestArgument])

        (mockBreakpointManager.createBreakpointRequestWithId _)
          .expects(*, *, *, *)
          .returning(expected).once()

        pendingBreakpointSupport.disablePendingSupport()
        val actual = pendingBreakpointSupport.createBreakpointRequest(
          testFileName, testLineNumber, extraArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#removeBreakpointRequestWithId") {
      it("should return true if the breakpoint was successfully deleted") {
        val expected = true

        val testFileName = "some/file/name"
        val testLineNumber = 1

        (mockBreakpointManager.createBreakpointRequestWithId _)
          .expects(TestRequestId, testFileName, testLineNumber, Nil)
          .returning(Success(TestRequestId)).once()

        // Set a breakpoint on a line that is returned by linesAndLocations
        pendingBreakpointSupport.createBreakpointRequest(
          testFileName,
          testLineNumber
        )

        (mockBreakpointManager.removeBreakpointRequestWithId _)
          .expects(TestRequestId)
          .returning(true).once()

        // Return "no removals" for pending breakpoints
        (mockPendingActionManager.removePendingActionsWithId _)
          .expects(TestRequestId)
          .returning(None).once()

        val actual = pendingBreakpointSupport.removeBreakpointRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should return true if the pending breakpoint was successfully deleted") {
        val expected = true

        val testFileName = "some/file/name"
        val testLineNumber = 1
        val error = NoBreakpointLocationFound(testFileName, testLineNumber)

        (mockBreakpointManager.createBreakpointRequestWithId _)
          .expects(TestRequestId, testFileName, testLineNumber, Nil)
          .returning(Failure(error)).once()

        // Pending breakpoint should be set
        (mockPendingActionManager.addPendingActionWithId _).expects(
          TestRequestId,
          BreakpointRequestInfo(testFileName, testLineNumber, Nil),
          * // Don't care about checking action
        ).returning(TestRequestId).once()

        // Set a breakpoint on a line that is returned by linesAndLocations
        pendingBreakpointSupport.createBreakpointRequest(
          testFileName,
          testLineNumber
        )

        // Return removals for pending breakpoints
        val pendingRemovalReturn = Some(Seq(
          ActionInfo(
            TestRequestId,
            BreakpointRequestInfo(testFileName, testLineNumber, Nil),
            () => {}
          )
        ))
        (mockBreakpointManager.removeBreakpointRequestWithId _)
          .expects(TestRequestId)
          .returning(false).once()
        (mockPendingActionManager.removePendingActionsWithId _)
          .expects(TestRequestId)
          .returning(pendingRemovalReturn).once()

        val actual = pendingBreakpointSupport.removeBreakpointRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should return false if the breakpoint was not found") {
        val expected = false

        (mockBreakpointManager.removeBreakpointRequestWithId _)
          .expects(TestRequestId)
          .returning(false).once()

        // Return "no removals" for pending breakpoints
        (mockPendingActionManager.removePendingActionsWithId _)
          .expects(TestRequestId)
          .returning(None).once()

        val actual = pendingBreakpointSupport.removeBreakpointRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#removeBreakpointRequest") {
      it("should return true if the breakpoint was successfully deleted") {
        val expected = true

        val testFileName = "some/file/name"
        val testLineNumber = 1

        (mockBreakpointManager.createBreakpointRequestWithId _)
          .expects(TestRequestId, testFileName, testLineNumber, Nil)
          .returning(Success(TestRequestId)).once()

        // Set a breakpoint on a line that is returned by linesAndLocations
        pendingBreakpointSupport.createBreakpointRequest(testFileName, 1)

        (mockBreakpointManager.removeBreakpointRequest _)
          .expects(testFileName, testLineNumber)
          .returning(true).once()

        // Return "no removals" for pending breakpoints (performed by standard
        // removeBreakpointRequest call)
        (mockPendingActionManager.removePendingActions _).expects(*)
          .returning(Nil).once()

        val actual = pendingBreakpointSupport.removeBreakpointRequest(
          testFileName,
          1
        )

        actual should be (expected)
      }

      it("should return true if the pending breakpoint was successfully deleted") {
        val expected = true

        val testFileName = "some/file/name"
        val testLineNumber = 1
        val error = NoBreakpointLocationFound(testFileName, testLineNumber)

        (mockBreakpointManager.createBreakpointRequestWithId _)
          .expects(TestRequestId, testFileName, testLineNumber, Nil)
          .returning(Failure(error)).once()

        // Pending breakpoint should be set
        (mockPendingActionManager.addPendingActionWithId _).expects(
          TestRequestId,
          BreakpointRequestInfo(testFileName, testLineNumber, Nil),
          * // Don't care about checking action
        ).returning(TestRequestId).once()

        // Set a breakpoint on a line that is returned by linesAndLocations
        pendingBreakpointSupport.createBreakpointRequest(
          testFileName,
          testLineNumber
        )

        // Return removals for pending breakpoints
        val pendingRemovalReturn = Seq(
          ActionInfo(
            TestRequestId,
            BreakpointRequestInfo(testFileName, testLineNumber, Nil),
            () => {}
          )
        )
        (mockBreakpointManager.removeBreakpointRequest _)
          .expects(testFileName, testLineNumber)
          .returning(false).once()
        (mockPendingActionManager.removePendingActions _)
          .expects(*)
          .returning(pendingRemovalReturn).once()

        val actual = pendingBreakpointSupport.removeBreakpointRequest(
          testFileName,
          testLineNumber
        )

        actual should be (expected)
      }

      it("should return false if the breakpoint was not found") {
        val expected = false
        val testFileName = "some/file/name"
        val testLineNumber = 1

        (mockBreakpointManager.removeBreakpointRequest _)
          .expects(testFileName, testLineNumber)
          .returning(false).once()

        // Return "no removals" for pending breakpoints
        (mockPendingActionManager.removePendingActions _).expects(*)
          .returning(Nil).once()

        val actual = pendingBreakpointSupport.removeBreakpointRequest(
          testFileName,
          testLineNumber
        )

        actual should be (expected)
      }
    }
  }
}
