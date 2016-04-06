package org.scaladebugger.api.profiles.traits.info

import com.sun.jdi.ThreadReference
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestGrabInfoProfile

class GrabInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  describe("GrabInfoProfile") {
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
