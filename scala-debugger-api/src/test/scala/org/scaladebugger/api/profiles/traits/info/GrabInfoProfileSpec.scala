package org.scaladebugger.api.profiles.traits.info

import com.sun.jdi.ThreadReference
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestGrabInfoProfile

import scala.util.Success

class GrabInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  describe("GrabInfoProfile") {
    describe("#findVariableByName") {
      it("should return None if no variable is found") {
        val expected = None
        val thread = mock[ThreadInfoProfile]
        val name = "someName"

        // Return 0 frames to test no variable found
        (thread.getTotalFrames _).expects().returning(0).once()

        val grabInfoProfile = new TestGrabInfoProfile
        val actual = grabInfoProfile.findVariableByName(thread, name)

        actual should be (expected)
      }

      it("should return Some(variable) if found") {
        val expected = Some(mock[VariableInfoProfile])
        val mockThread = mock[ThreadInfoProfile]
        val name = "someName"

        val mockFrame = mock[FrameInfoProfile]
        (mockThread.getTotalFrames _).expects().returning(1).once()
        (mockThread.getFrame _).expects(0).returning(mockFrame).once()
        (mockFrame.tryGetVariable _).expects(name)
          .returning(Success(expected.get)).once()

        val grabInfoProfile = new TestGrabInfoProfile
        val actual = grabInfoProfile.findVariableByName(mockThread, name)

        actual should be (expected)
      }
    }

    describe("#tryFindVariableByName") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ThreadInfoProfile, String, Option[VariableInfoProfile]]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def findVariableByName(
            thread: ThreadInfoProfile,
            name: String
          ): Option[VariableInfoProfile] = mockUnsafeMethod(thread, name)
        }

        val a1 = mock[ThreadInfoProfile]
        val a2 = "someName"
        val r = Some(mock[VariableInfoProfile])
        mockUnsafeMethod.expects(a1, a2).returning(r).once()
        grabInfoProfile.tryFindVariableByName(a1, a2).get should be (r.get)
      }
    }

    describe("#tryGetThread(threadId)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Long, ThreadInfoProfile]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def getThread(threadId: Long): ThreadInfoProfile =
            mockUnsafeMethod(threadId)
        }

        val a1 = 999L
        val r = mock[ThreadInfoProfile]
        mockUnsafeMethod.expects(a1).returning(r).once()
        grabInfoProfile.tryGetThread(a1).get should be (r)
      }
    }

    describe("#tryGetThread(threadReference)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ThreadReference, ThreadInfoProfile]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def getThread(threadReference: ThreadReference): ThreadInfoProfile =
            mockUnsafeMethod(threadReference)
        }

        val a1 = mock[ThreadReference]
        val r = mock[ThreadInfoProfile]
        mockUnsafeMethod.expects(a1).returning(r).once()
        grabInfoProfile.tryGetThread(a1).get should be (r)
      }
    }

    describe("#tryGetClasses") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[ReferenceTypeInfoProfile]]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def getClasses: Seq[ReferenceTypeInfoProfile] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[ReferenceTypeInfoProfile])
        mockUnsafeMethod.expects().returning(r).once()
        grabInfoProfile.tryGetClasses.get should be (r)
      }
    }
  }
}
