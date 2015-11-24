package org.senkbeil.debugger.api.lowlevel.breakpoints

import com.sun.jdi.{Location, VirtualMachine}
import com.sun.jdi.request.{EventRequest, BreakpointRequest, EventRequestManager}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.classes.ClassManager
import test.JDIMockHelpers

import scala.util.{Success, Failure}

class BreakpointManagerSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockEventRequestManager = mock[EventRequestManager]
  private val stubVirtualMachine = stub[VirtualMachine]

  // NOTE: Needed until https://github.com/paulbutcher/ScalaMock/issues/56
  class ZeroArgClassManager
    extends ClassManager(stubVirtualMachine, loadClasses = false)
  private val mockClassManager = mock[ZeroArgClassManager]

  private val breakpointManager = new BreakpointManager(
    mockEventRequestManager,
    mockClassManager
  ) {
    override protected def newRequestId(): String = TestRequestId
  }

  describe("BreakpointManager") {
    describe("#breakpointRequestListById") {
      it("should return a collection of breakpoint file names and lines") {
        val expected = Seq(
          ("id1", "file1", 1),
          ("id2", "file1", 2),
          ("id3", "file2", 999)
        )

        // Build the map to return from linesAndLocationsForFile(...)
        (mockClassManager.linesAndLocationsForFile _).expects(*).onCall(
          (fileName: String) => Some(expected
            .map(t => (t._2, t._3))
            .groupBy(_._1)(fileName)
            .map(_._2)
            .map(i => (i, Seq(createRandomLocationStub())))
            .toMap)
        ).repeated(expected.length).times()

        // Stub out the call to create a breakpoint request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .returning(stub[BreakpointRequest]).repeated(expected.length).times()

        expected.foreach(b =>
          breakpointManager.createLineBreakpointRequestWithId(b._1, b._2, b._3)
        )

        val actual = breakpointManager.breakpointRequestListById

        actual should contain theSameElementsAs expected.map(_._1)
      }

      it("should return an empty collection if no breakpoints have been set") {
        breakpointManager.breakpointRequestListById should be (empty)
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

    describe("#createLineBreakpointRequestWithId") {
      it("should create a new breakpoint request using the provided id") {
        val expected = Success(java.util.UUID.randomUUID().toString)
        val testFileName = "some/file/name"
        val testLineNumber = 1

        // Mark the retrieval of lines and locations to a map with
        // a line number that will be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(Some(Map(1 -> Seq(createRandomLocationStub()))))

        // Stub out the call to create a breakpoint request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .returning(stub[BreakpointRequest])

        val actual = breakpointManager.createLineBreakpointRequestWithId(
          expected.get,
          testFileName,
          testLineNumber
        )

        actual should be(expected)
      }
    }

    describe("#createLineBreakpointRequest") {
      it("should return NoBreakpointLocationFound if the file is not available") {
        val testFileName = "some/file/name"
        val testLineNumber = 999
        val expected = Failure(NoBreakpointLocationFound(
          testFileName, testLineNumber
        ))

        // Mark the retrieval of lines and locations to indicate no file
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(None)

        val actual = breakpointManager.createLineBreakpointRequest(
          testFileName,
          testLineNumber
        )

        actual should be (expected)
      }

      it("should return NoBreakpointLocationFound if the line is not available") {
        val testFileName = "some/file/name"
        val testLineNumber = 999
        val expected = Failure(NoBreakpointLocationFound(
          testFileName, testLineNumber
        ))

        // Mark the retrieval of lines and locations to a map with
        // a line number that will NOT be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(Some(Map(1 -> (Nil: Seq[Location]))))

        // Set a breakpoint on a line that is NOT returned by linesAndLocations
        val actual = breakpointManager.createLineBreakpointRequest(
          testFileName,
          testLineNumber
        )

        actual should be (expected)
      }

      it("should return Success(id) if successfully added the breakpoint") {
        val expected = Success(TestRequestId)

        // Mark the retrieval of lines and locations to a map with
        // a line number that will be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(Some(Map(1 -> Seq(createRandomLocationStub()))))

        // Stub out the call to create a breakpoint request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .returning(stub[BreakpointRequest])

        // Set a breakpoint on a line that is returned by linesAndLocations
        val actual = breakpointManager.createLineBreakpointRequest("", 1)

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

    describe("#hasLineBreakpointRequestWithId") {
      it("should return true if a breakpoint request exists with the id") {
        val expected = true

        // Mark the retrieval of lines and locations to a map with
        // a line number that will be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(Some(Map(1 -> Seq(createRandomLocationStub()))))

        // Stub out the call to create a breakpoint request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .returning(stub[BreakpointRequest])

        // Set a breakpoint on a line that is returned by linesAndLocations
        breakpointManager.createLineBreakpointRequestWithId(
          TestRequestId,
          "file",
          1
        )

        // Verify that we have the file and line in our list
        val actual = breakpointManager.hasLineBreakpointRequestWithId(TestRequestId)

        actual should be (expected)
      }

      it("should return true if a breakpoint request does not exist with the id") {
        val expected = false

        val actual = breakpointManager.hasLineBreakpointRequestWithId(TestRequestId)

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

    describe("#getLineBreakpointRequestWithId") {
      it("should return Some(breakpoint requests) if the id matches something") {
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
        breakpointManager.createLineBreakpointRequestWithId(
          TestRequestId,
          "file",
          1
        )

        // Should not be empty
        val actual = breakpointManager
          .getLineBreakpointRequestWithId(TestRequestId)
          .get

        actual should be (expected)
      }

      it("should return None if no breakpoint with the id is found") {
        breakpointManager.getLineBreakpointRequestWithId(TestRequestId) should
          be (None)
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

    describe("#removeLineBreakpointRequestWithId") {
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
        breakpointManager.createLineBreakpointRequestWithId(
          TestRequestId,
          "file",
          1
        )

        // Should remove X breakpoint requests through one call
        (mockEventRequestManager.deleteEventRequests _).expects(where {
          l: java.util.List[_ <: EventRequest] =>
            l.size == totalBreakpointRequests
        }).once()

        val actual =
          breakpointManager.removeLineBreakpointRequestWithId(TestRequestId)

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
        breakpointManager.createLineBreakpointRequestWithId(
          TestRequestId,
          "file",
          1
        )

        // Should remove X breakpoint requests through one call
        (mockEventRequestManager.deleteEventRequests _).expects(where {
          l: java.util.List[_ <: EventRequest] =>
            l.size == totalBreakpointRequests
        }).once()

        breakpointManager.removeLineBreakpointRequestWithId(TestRequestId)
      }

      it("should return false if the breakpoint was not found") {
        val expected = false
        val actual =
          breakpointManager.removeLineBreakpointRequestWithId(TestRequestId)

        actual should be (expected)
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

      it("should return false if the breakpoint was not found") {
        val expected = false
        val actual = breakpointManager.removeLineBreakpointRequest("file", 1)

        actual should be (expected)
      }
    }
  }
}
