package org.scaladebugger.api.profiles.traits.info

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestFrameInfoProfile

import scala.util.{Failure, Success, Try}

class FrameInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  describe("FrameInfoProfile") {
    describe("#toPrettyString") {
      it("should include the frame's location if available") {
        val expected = "Frame at (LOCATION)"
        val mockUnsafeMethod = mockFunction[Try[LocationInfoProfile]]

        val frameInfoProfile = new TestFrameInfoProfile {
          override def tryGetLocation: Try[LocationInfoProfile] =
            mockUnsafeMethod()
        }

        val r = Success(mock[LocationInfoProfile])
        mockUnsafeMethod.expects().returning(r).once()
        (r.get.toPrettyString _).expects().returning("LOCATION").once()

        val actual = frameInfoProfile.toPrettyString

        actual should be (expected)
      }

      it("should use ??? if the frame's location is unavailable") {
        val expected = "Frame at (???)"
        val mockUnsafeMethod = mockFunction[Try[LocationInfoProfile]]

        val frameInfoProfile = new TestFrameInfoProfile {
          override def tryGetLocation: Try[LocationInfoProfile] =
            mockUnsafeMethod()
        }

        val r = Failure(new Throwable)
        mockUnsafeMethod.expects().returning(r).once()

        val actual = frameInfoProfile.toPrettyString

        actual should be (expected)
      }
    }

    describe("#tryGetThisObject") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ObjectInfoProfile]

        val frameInfoProfile = new TestFrameInfoProfile {
          override def getThisObject: ObjectInfoProfile = mockUnsafeMethod()
        }

        val r = mock[ObjectInfoProfile]
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryGetThisObject.get should be (r)
      }
    }

    describe("#tryGetCurrentThread") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ThreadInfoProfile]

        val frameInfoProfile = new TestFrameInfoProfile {
          override def getCurrentThread: ThreadInfoProfile =
            mockUnsafeMethod()
        }

        val r = mock[ThreadInfoProfile]
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryGetCurrentThread.get should be (r)
      }
    }

    describe("#tryGetLocation") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[LocationInfoProfile]

        val frameInfoProfile = new TestFrameInfoProfile {
          override def getLocation: LocationInfoProfile =
            mockUnsafeMethod()
        }

        val r = mock[LocationInfoProfile]
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryGetLocation.get should be (r)
      }
    }

    describe("#tryGetVariable") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[String, VariableInfoProfile]

        val frameInfoProfile = new TestFrameInfoProfile {
          override def getVariable(name: String): VariableInfoProfile =
            mockUnsafeMethod(name)
        }

        val a1 = "someName"
        val r = mock[VariableInfoProfile]
        mockUnsafeMethod.expects(a1).returning(r).once()
        frameInfoProfile.tryGetVariable(a1).get should be (r)
      }
    }

    describe("#tryGetFieldVariables") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[VariableInfoProfile]]

        val frameInfoProfile = new TestFrameInfoProfile {
          override def getFieldVariables: Seq[VariableInfoProfile] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[VariableInfoProfile])
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryGetFieldVariables.get should be (r)
      }
    }

    describe("#tryGetLocalVariables") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[VariableInfoProfile]]

        val frameInfoProfile = new TestFrameInfoProfile {
          override def getLocalVariables: Seq[VariableInfoProfile] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[VariableInfoProfile])
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryGetLocalVariables.get should be (r)
      }
    }

    describe("#tryGetAllVariables") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[VariableInfoProfile]]

        val frameInfoProfile = new TestFrameInfoProfile {
          override def getAllVariables: Seq[VariableInfoProfile] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[VariableInfoProfile])
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryGetAllVariables.get should be (r)
      }
    }

    describe("#tryGetArguments") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[VariableInfoProfile]]

        val frameInfoProfile = new TestFrameInfoProfile {
          override def getArguments: Seq[VariableInfoProfile] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[VariableInfoProfile])
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryGetArguments.get should be (r)
      }
    }

    describe("#tryGetNonArguments") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[VariableInfoProfile]]

        val frameInfoProfile = new TestFrameInfoProfile {
          override def getNonArguments: Seq[VariableInfoProfile] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[VariableInfoProfile])
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryGetNonArguments.get should be (r)
      }
    }
  }
}
