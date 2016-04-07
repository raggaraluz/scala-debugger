package org.scaladebugger.api.profiles.traits.info

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestFrameInfoProfile

class FrameInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  describe("FrameInfoProfile") {
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
