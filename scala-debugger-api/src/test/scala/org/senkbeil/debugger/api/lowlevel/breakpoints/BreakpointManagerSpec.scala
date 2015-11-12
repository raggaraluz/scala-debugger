package org.senkbeil.debugger.api.lowlevel.breakpoints

import com.sun.jdi.{Location, VirtualMachine}
import com.sun.jdi.request.{EventRequest, BreakpointRequest, EventRequestManager}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.classes.ClassManager
import test.JDIMockHelpers

import scala.util.Failure

class BreakpointManagerSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory with JDIMockHelpers
{
  private val mockEventRequestManager = mock[EventRequestManager]
  private val stubVirtualMachine = stub[VirtualMachine]

  // NOTE: Needed until https://github.com/paulbutcher/ScalaMock/issues/56
  class ZeroArgClassManager
    extends ClassManager(stubVirtualMachine, loadClasses = false)
  private val mockClassManager = mock[ZeroArgClassManager]

  private val breakpointManager = new BreakpointManager(
    mockEventRequestManager,
    mockClassManager
  )

  describe("BreakpointManager") {
    describe("#pendingBreakpointList") {
      it("should return a collection of pending breakpoint file names and lines") {
        val expected = Seq(("file1", 1), ("file1", 2), ("file2", 999))

        // An empty or invalid list of lines/locations yields a
        // pending breakpoint
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(None).repeated(expected.length).times()

        expected.foreach(b => breakpointManager.createLineBreakpointRequest(b._1, b._2))

        val actual = breakpointManager.pendingBreakpointList

        actual should contain theSameElementsAs expected
      }
    }

    describe("#breakpointRequestList") {
      it("should return a collection of breakpoint file names and lines") {
        val expected = Seq(("file1", 1), ("file1", 2), ("file2", 999))

        // Build the map to return from linesAndLocationsForFile(...)
        (mockClassManager.linesAndLocationsForFile _).expects(*).onCall(
          (fileName: String) => Some(expected
            .groupBy(_._1)(fileName)
            .map(_._2)
            .map(i => (i, Seq(createRandomLocationStub())))
            .toMap)
        ).repeated(expected.length).times()

        // Stub out the call to create a breakpoint request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .returning(stub[BreakpointRequest]).repeated(expected.length).times()

        expected.foreach(b => breakpointManager.createLineBreakpointRequest(b._1, b._2))

        val actual = breakpointManager.breakpointRequestList

        actual should contain theSameElementsAs expected
      }

      it("should return an empty collection if no breakpoints have been set") {
        breakpointManager.breakpointRequestList should be (empty)
      }
    }

    describe("#processPendingBreakpoints") {
      it("should return true if all pending breakpoints are added successfully") {
        val expected = true
        val fileName = "some file"
        val lineNumber = 2

        // = ADD PENDING BREAKPOINT ============================================

        // Mark the retrieval of lines and locations to a map with
        // a line number that will not be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*).returning(
          Some(Map((lineNumber + 1) -> Seq(createRandomLocationStub())))
        ).once()

        // Set a breakpoint on a line that is NOT returned by linesAndLocations,
        // which results in adding the pending breakpoint
        breakpointManager.createLineBreakpointRequest(fileName, lineNumber)

        // = PROCESS PENDING BREAKPOINT ========================================

        // Mark the retrieval of lines and locations to a map with
        // a line number that will be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*).returning(
          Some(Map(lineNumber -> Seq(createRandomLocationStub())))
        ).once()

        // Stub out the call to create a breakpoint request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .returning(stub[BreakpointRequest]).once()

        val actual = breakpointManager.processPendingBreakpoints(fileName)

        actual should be (expected)
      }

      it("should return false if a pending breakpoint was not added") {
        val expected = false
        val fileName = "some file"
        val lineNumber = 2

        // = ADD PENDING BREAKPOINT ============================================

        // Mark the retrieval of lines and locations to a map with
        // a line number that will not be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*).returning(
          Some(Map((lineNumber + 1) -> Seq(createRandomLocationStub())))
        ).once()

        // Set a breakpoint on a line that is NOT returned by linesAndLocations,
        // which results in adding the pending breakpoint
        breakpointManager.createLineBreakpointRequest(fileName, lineNumber)

        // = PROCESS PENDING BREAKPOINT ========================================

        // Mark the retrieval of lines and locations to a map with
        // a line number that will not be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*).returning(
          Some(Map((lineNumber + 1) -> Seq(createRandomLocationStub())))
        ).once()

        val actual = breakpointManager.processPendingBreakpoints(fileName)

        actual should be (expected)
      }

      it("should return true if there are no pending breakpoints") {
        val expected = true
        val actual = breakpointManager.processPendingBreakpoints("some file")

        actual should be (expected)
      }
    }

    describe("#createLineBreakpointRequest") {
      it("should return false if the file is not available") {
        val expected = false

        // Mark the retrieval of lines and locations to indicate no file
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(None)

        val actual = breakpointManager.createLineBreakpointRequest("", 0).get

        actual should be (expected)
      }

      it("should return false if the line is not available") {
        val expected = false

        // Mark the retrieval of lines and locations to a map with
        // a line number that will NOT be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(Some(Map(1 -> (Nil: Seq[Location]))))

        // Set a breakpoint on a line that is NOT returned by linesAndLocations
        val actual = breakpointManager.createLineBreakpointRequest("", 0).get

        actual should be (expected)
      }

      it("should return true if successfully added the breakpoint") {
        val expected = true

        // Mark the retrieval of lines and locations to a map with
        // a line number that will be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(Some(Map(1 -> Seq(createRandomLocationStub()))))

        // Stub out the call to create a breakpoint request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .returning(stub[BreakpointRequest])

        // Set a breakpoint on a line that is returned by linesAndLocations
        val actual = breakpointManager.createLineBreakpointRequest("", 1).get

        actual should be (expected)
      }

      it("should create a new breakpoint request for each matching location") {
        val locationsPerLine = 3

        // Mark the retrieval of lines and locations to a map with
        // a line number that will be the one picked and one that will not
        (mockClassManager.linesAndLocationsForFile _).expects(*).returning(
          Some(Map(
            0 -> (1 to locationsPerLine).map(_ => createRandomLocationStub()),
            1 -> (1 to locationsPerLine).map(_ => createRandomLocationStub())
          ))
        )

        // Stub out the call to create a breakpoint request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .returning(stub[BreakpointRequest])
          .repeated(locationsPerLine).times()

        // Set a breakpoint on a line that is returned by linesAndLocations
        breakpointManager.createLineBreakpointRequest("", 1)
      }

      it("should return the failure if unable to create one of the underlying breakpoint requests") {
        val expected = Failure(new Throwable)
        val locationsPerLine = 3

        // Mark the retrieval of lines and locations to a map with
        // a line number that will be the one picked and one that will not
        (mockClassManager.linesAndLocationsForFile _).expects(*).returning(
          Some(Map(
            0 -> (1 to locationsPerLine).map(_ => createRandomLocationStub()),
            1 -> (1 to locationsPerLine).map(_ => createRandomLocationStub())
          ))
        )

        // Stub out the call to create a breakpoint request (except last)
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .returning(stub[BreakpointRequest])
          .repeated(locationsPerLine - 1).times()

        // TODO: Does this actually throw an exception?
        // Stub out the call to create last breakpoint request to
        // throw an exception
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .throwing(expected.failed.get).once()

        // Set a breakpoint on a line that is returned by linesAndLocations
        val actual = breakpointManager.createLineBreakpointRequest("", 1)

        actual should be (expected)
      }
    }

    describe("#hasLineBreakpointRequest") {
      it("should return true if the breakpoint with matching file name and line is found") {
        val expected = true

        // Mark the retrieval of lines and locations to a map with
        // a line number that will be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(Some(Map(1 -> Seq(createRandomLocationStub()))))

        // Stub out the call to create a breakpoint request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .returning(stub[BreakpointRequest])

        // Set a breakpoint on a line that is returned by linesAndLocations
        breakpointManager.createLineBreakpointRequest("file", 1)

        // Verify that we have the file and line in our list
        val actual = breakpointManager.hasLineBreakpointRequest("file", 1)

        actual should be (expected)
      }

      it("should return false if no breakpoint is found") {
        val expected = false

        val actual = breakpointManager.hasLineBreakpointRequest("file", 1)

        actual should be (expected)
      }
    }

    describe("#getLineBreakpointRequest") {
      it("should return Some(collection of breakpoints representing the line)") {
        val stubBreakpointRequest = stub[BreakpointRequest]
        val expected = Seq(stubBreakpointRequest)

        // Mark the retrieval of lines and locations to a map with
        // a line number that will be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(Some(Map(1 -> Seq(createRandomLocationStub()))))

        // Stub out the call to create a breakpoint request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .returning(stubBreakpointRequest)

        // Set a breakpoint on a line that is returned by linesAndLocations
        breakpointManager.createLineBreakpointRequest("file", 1)

        // Should not be empty
        val actual = breakpointManager
          .getLineBreakpointRequest("file", 1)
          .get

        actual should be (expected)
      }

      it("should return None if no breakpoint is found") {
        breakpointManager.getLineBreakpointRequest("file", 1) should be (None)
      }
    }

    describe("#removeLineBreakpointRequest") {
      it("should return true if the breakpoint was successfully deleted") {
        val expected = true
        val totalBreakpointRequests = 3

        // Create X locations that will result in X breakpoint requests
        val locations = (1 to totalBreakpointRequests)
          .map(_ => createRandomLocationStub())

        // Mark the retrieval of lines and locations to a map with
        // a line number that will be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(Some(Map(1 -> locations)))

        // Stub out the call to create a breakpoint request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .returning(stub[BreakpointRequest])
          .repeated(totalBreakpointRequests).times()

        // Set a breakpoint on a line that is returned by linesAndLocations
        breakpointManager.createLineBreakpointRequest("file", 1)

        // Should remove X breakpoint requests through one call
        (mockEventRequestManager.deleteEventRequests _).expects(where {
          l: java.util.List[_ <: EventRequest] =>
            l.size == totalBreakpointRequests
        }).once()

        val actual = breakpointManager.removeLineBreakpointRequest("file", 1)

        actual should be (expected)
      }

      it("should delete each breakpoint request represented by the bundle") {
        val totalBreakpointRequests = 3

        // Create X locations that will result in X breakpoint requests
        val locations = (1 to totalBreakpointRequests)
          .map(_ => createRandomLocationStub())

        // Mark the retrieval of lines and locations to a map with
        // a line number that will be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(Some(Map(1 -> locations)))

        // Stub out the call to create a breakpoint request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .returning(stub[BreakpointRequest])
          .repeated(totalBreakpointRequests).times()

        // Set a breakpoint on a line that is returned by linesAndLocations
        breakpointManager.createLineBreakpointRequest("file", 1)

        // Should remove X breakpoint requests through one call
        (mockEventRequestManager.deleteEventRequests _).expects(where {
          l: java.util.List[_ <: EventRequest] =>
            l.size == totalBreakpointRequests
        }).once()

        breakpointManager.removeLineBreakpointRequest("file", 1)
      }

      it("should delete all pending breakpoints matching the file and line") {
        val expected = Seq(("file1", 1), ("file2", 999))
        val breakpointToDelete = ("file1", 2)

        // An empty or invalid list of lines/locations yields a
        // pending breakpoint
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(None).repeated(expected.length + 1).times()

        // Add all of our breakpoints
        expected.foreach(b => breakpointManager.createLineBreakpointRequest(b._1, b._2))
        breakpointManager.createLineBreakpointRequest(
          breakpointToDelete._1,
          breakpointToDelete._2
        )

        // Remove our breakpoint
        breakpointManager.removeLineBreakpointRequest(
          breakpointToDelete._1,
          breakpointToDelete._2
        )

        val actual = breakpointManager.pendingBreakpointList
        actual should contain theSameElementsAs expected
      }

      it("should return false if the breakpoint was not found") {
        val expected = false
        val actual = breakpointManager.removeLineBreakpointRequest("file", 1)

        actual should be (expected)
      }
    }
  }
}
