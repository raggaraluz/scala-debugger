package org.senkbeil.debugger.api.lowlevel.breakpoints

import com.sun.jdi.request.{BreakpointRequest, EventRequest, EventRequestManager}
import com.sun.jdi.{Location, VirtualMachine}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.classes.ClassManager
import org.senkbeil.debugger.api.lowlevel.utils.{ActionInfo, PendingActionManager}
import test.JDIMockHelpers

import scala.util.{Failure, Success}

class ExtendedBreakpointManagerSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockEventRequestManager = mock[EventRequestManager]
  private val stubVirtualMachine = stub[VirtualMachine]

  // NOTE: Needed until https://github.com/paulbutcher/ScalaMock/issues/56
  class ZeroArgClassManager
    extends ClassManager(stubVirtualMachine, loadClasses = false)
  private val mockClassManager = mock[ZeroArgClassManager]

  private class TestBreakpointInfoPendingActionManager
    extends PendingActionManager[BreakpointInfo]
  private val mockPendingActionManager =
    mock[TestBreakpointInfoPendingActionManager]

  private val extendedBreakpointManager = new ExtendedBreakpointManager(
    mockEventRequestManager,
    mockClassManager,
    mockPendingActionManager
  ) {
    override protected def newRequestId(): String = TestRequestId
  }

  describe("ExtendedBreakpointManager") {
    describe("#processPendingBreakpointsForFile") {
      it("should process pending breakpoints for the specified file") {
        val testFileName = "some/file/name"
        val testLineNumber = 1

        val expected = Seq(
          BreakpointInfo(testFileName, testLineNumber, Nil)
        )

        // Mark the retrieval of lines and locations to a map with
        // a line number that will be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(Some(Map(1 -> Seq(createRandomLocationStub())))).once()

        // Stub out the call to create a breakpoint request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .returning(stub[BreakpointRequest]).once()

        // Return our data that represents the expected file and line
        (mockPendingActionManager.processActions _).expects(*)
          .returning(Seq(ActionInfo("id", expected.head, () => {})))
          .once()

        extendedBreakpointManager.createBreakpointRequest(
          testFileName,
          testLineNumber
        )

        val actual = extendedBreakpointManager.processPendingBreakpointsForFile(
          testFileName
        )

        actual should be (expected)
      }
    }

    describe("#pendingBreakpointsForFile") {
      it("should return a collection of pending breakpoints") {
        val testFileName = "some/file/name"
        val testLineNumber = 1

        val expected = Seq((testFileName, testLineNumber))

        // Mark the retrieval of lines and locations to a map with
        // a line number that will be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(Some(Map(1 -> Seq(createRandomLocationStub())))).once()

        // Stub out the call to create a breakpoint request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .returning(stub[BreakpointRequest]).once()

        // Return our data that represents the expected file and line
        (mockPendingActionManager.getPendingActionData _).expects(*)
          .returning(Seq(BreakpointInfo(testFileName, testLineNumber, Nil)))
          .once()

        extendedBreakpointManager.createBreakpointRequest(
          testFileName,
          testLineNumber
        )

        val actual =
          extendedBreakpointManager.pendingBreakpointsForFile(testFileName)

        actual should be (expected)
      }

      it("should be empty if there are no pending breakpoints") {
        val expected = Nil

        // No pending breakpoints
        (mockPendingActionManager.getPendingActionData _).expects(*)
          .returning(Nil).once()

        val actual = extendedBreakpointManager.pendingBreakpointsForFile("file")

        actual should be (expected)
      }
    }

    describe("#createBreakpointRequestWithId") {
      it("should return Success(id) if the breakpoint was created") {
        val testFileName = "some/file/name"
        val testLineNumber = 1

        val expected = Success(TestRequestId)

        // Mark the retrieval of lines and locations to a map with
        // a line number that will be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(Some(Map(1 -> Seq(createRandomLocationStub())))).once()

        // Stub out the call to create a breakpoint request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .returning(stub[BreakpointRequest]).once()

        val actual = extendedBreakpointManager.createBreakpointRequestWithId(
          TestRequestId,
          testFileName,
          testLineNumber
        )

        actual should be (expected)
      }

      it("should return Failure(ex) if an error was thrown") {
        val testFileName = "some/file/name"
        val testLineNumber = 1

        val expected = Failure(new Throwable)

        // Mark the retrieval of lines and locations to a map with
        // a line number that will be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(Some(Map(1 -> Seq(createRandomLocationStub())))).once()

        // Throw an exception when attempting to create the request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .throwing(expected.exception).once()

        val actual = extendedBreakpointManager.createBreakpointRequestWithId(
          TestRequestId,
          testFileName,
          testLineNumber
        )

        actual should be (expected)
      }

      it("should add a pending breakpoint if NoBreakpointLocationFound thrown") {
        val testFileName = "some/file/name"
        val testLineNumber = 1

        val expected = Failure(new NoBreakpointLocationFound(
          testFileName, testLineNumber
        ))

        // Mark the retrieval of lines and locations to a map with
        // a line number that will be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(Some(Map(1 -> Seq(createRandomLocationStub())))).once()

        // Throw an exception when attempting to create the request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .throwing(expected.exception).once()

        // Pending breakpoint should be set
        (mockPendingActionManager.addPendingActionWithId _).expects(
          TestRequestId,
          BreakpointInfo(testFileName, testLineNumber, Nil),
          * // Don't care about checking action
        ).returning(TestRequestId).once()

        val actual = extendedBreakpointManager.createBreakpointRequestWithId(
          TestRequestId,
          testFileName,
          testLineNumber
        )

        actual should be (expected)
      }
    }

    describe("#createBreakpointRequest") {
      it("should return Success(id) if the breakpoint was created") {
        val testFileName = "some/file/name"
        val testLineNumber = 1

        val expected = Success(TestRequestId)

        // Mark the retrieval of lines and locations to a map with
        // a line number that will be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(Some(Map(1 -> Seq(createRandomLocationStub())))).once()

        // Stub out the call to create a breakpoint request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .returning(stub[BreakpointRequest]).once()

        val actual = extendedBreakpointManager.createBreakpointRequest(
          testFileName,
          testLineNumber
        )

        actual should be (expected)
      }

      it("should return Failure(ex) if an error was thrown") {
        val testFileName = "some/file/name"
        val testLineNumber = 1

        val expected = Failure(new Throwable)

        // Mark the retrieval of lines and locations to a map with
        // a line number that will be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(Some(Map(1 -> Seq(createRandomLocationStub())))).once()

        // Throw an exception when attempting to create the request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .throwing(expected.exception).once()

        val actual = extendedBreakpointManager.createBreakpointRequest(
          testFileName,
          testLineNumber
        )

        actual should be (expected)
      }

      it("should add a pending breakpoint if NoBreakpointLocationFound thrown") {
        val testFileName = "some/file/name"
        val testLineNumber = 1

        val expected = Failure(new NoBreakpointLocationFound(
          testFileName, testLineNumber
        ))

        // Mark no location found
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(None).once()

        // Pending breakpoint should be set
        (mockPendingActionManager.addPendingActionWithId _).expects(
          TestRequestId,
          BreakpointInfo(testFileName, testLineNumber, Nil),
          * // Don't care about checking action
        ).returning(TestRequestId).once()

        val actual = extendedBreakpointManager.createBreakpointRequest(
          testFileName,
          testLineNumber
        )

        actual should be (expected)
      }
    }

    describe("#removeBreakpointRequestWithId") {
      it("should return true if the breakpoint was successfully deleted") {
        val expected = true
        val totalBreakpointRequests = 3

        // Create X locations that will result in X breakpoint requests
        val locations = (1 to totalBreakpointRequests)
          .map(_ => createRandomLocationStub())

        // Mark the retrieval of lines and locations to a map with
        // a line number that will be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(Some(Map(1 -> locations))).once()

        // Stub out the call to create a breakpoint request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .returning(stub[BreakpointRequest])
          .repeated(totalBreakpointRequests).times()

        // Set a breakpoint on a line that is returned by linesAndLocations
        extendedBreakpointManager.createBreakpointRequest("file", 1)

        // Should remove X breakpoint requests through one call
        (mockEventRequestManager.deleteEventRequests _).expects(where {
          l: java.util.List[_ <: EventRequest] =>
            l.size == totalBreakpointRequests
        }).once()

        // Return "no removals" for pending breakpoints
        (mockPendingActionManager.removePendingActionsWithId _)
          .expects(TestRequestId)
          .returning(None).once()

        val actual = extendedBreakpointManager.removeBreakpointRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should return true if the pending breakpoint was successfully deleted") {
        val expected = true

        val testFileName = "some/file/name"
        val testLineNumber = 1

        // Mark no location found
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(None).once()

        // Pending breakpoint should be set
        (mockPendingActionManager.addPendingActionWithId _).expects(
          TestRequestId,
          BreakpointInfo(testFileName, testLineNumber, Nil),
          * // Don't care about checking action
        ).returning(TestRequestId).once()

        // Set a breakpoint on a line that is returned by linesAndLocations
        extendedBreakpointManager.createBreakpointRequest(
          testFileName,
          testLineNumber
        )

        // Return removals for pending breakpoints
        val pendingRemovalReturn = Some(Seq(
          ActionInfo(
            TestRequestId,
            BreakpointInfo(testFileName, testLineNumber, Nil),
            () => {}
          )
        ))
        (mockPendingActionManager.removePendingActionsWithId _)
          .expects(TestRequestId)
          .returning(pendingRemovalReturn).once()

        val actual = extendedBreakpointManager.removeBreakpointRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }

      it("should return false if the breakpoint was not found") {
        val expected = false

        // Return "no removals" for pending breakpoints
        (mockPendingActionManager.removePendingActionsWithId _)
          .expects(TestRequestId)
          .returning(None).once()

        val actual = extendedBreakpointManager.removeBreakpointRequestWithId(
          TestRequestId
        )

        actual should be (expected)
      }
    }

    describe("#removeBreakpointRequest") {
      it("should return true if the breakpoint was successfully deleted") {
        val expected = true
        val totalBreakpointRequests = 3
        val testFileName = "some/file/name"

        // Create X locations that will result in X breakpoint requests
        val locations = (1 to totalBreakpointRequests)
          .map(_ => createRandomLocationStub())

        // Mark the retrieval of lines and locations to a map with
        // a line number that will be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(Some(Map(1 -> locations))).once()

        // Stub out the call to create a breakpoint request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .returning(stub[BreakpointRequest])
          .repeated(totalBreakpointRequests).times()

        // Set a breakpoint on a line that is returned by linesAndLocations
        extendedBreakpointManager.createBreakpointRequest(testFileName, 1)

        // Should remove X breakpoint requests through one call
        (mockEventRequestManager.deleteEventRequests _).expects(where {
          l: java.util.List[_ <: EventRequest] =>
            l.size == totalBreakpointRequests
        }).once()

        // Return "no removals" for pending breakpoints (performed by standard
        // removeBreakpointRequestWithId call that removeBreakpointRequest
        // delegates to)
        (mockPendingActionManager.removePendingActionsWithId _)
          .expects(TestRequestId)
          .returning(None).once()

        // Return "no removals" for pending breakpoints (performed by standard
        // removeBreakpointRequest call)
        (mockPendingActionManager.removePendingActions _).expects(*)
          .returning(Nil).once()

        val actual = extendedBreakpointManager.removeBreakpointRequest(
          testFileName,
          1
        )

        actual should be (expected)
      }

      it("should return true if the pending breakpoint was successfully deleted") {
        val expected = true

        val testFileName = "some/file/name"
        val testLineNumber = 1

        // Mark no location found
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(None).once()

        // Pending breakpoint should be set
        (mockPendingActionManager.addPendingActionWithId _).expects(
          TestRequestId,
          BreakpointInfo(testFileName, testLineNumber, Nil),
          * // Don't care about checking action
        ).returning(TestRequestId).once()

        // Set a breakpoint on a line that is returned by linesAndLocations
        extendedBreakpointManager.createBreakpointRequest(
          testFileName,
          testLineNumber
        )

        // Return removals for pending breakpoints
        val pendingRemovalReturn = Seq(
          ActionInfo(
            TestRequestId,
            BreakpointInfo(testFileName, testLineNumber, Nil),
            () => {}
          )
        )
        (mockPendingActionManager.removePendingActions _)
          .expects(*)
          .returning(pendingRemovalReturn).once()

        val actual = extendedBreakpointManager.removeBreakpointRequest(
          testFileName,
          testLineNumber
        )

        actual should be (expected)
      }

      it("should return false if the breakpoint was not found") {
        val expected = false

        // Return "no removals" for pending breakpoints
        (mockPendingActionManager.removePendingActions _).expects(*)
          .returning(Nil).once()

        val actual = extendedBreakpointManager.removeBreakpointRequest(
          "file",
          1
        )

        actual should be (expected)
      }
    }
  }
}
