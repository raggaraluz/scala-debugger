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
    describe("#tryThread(threadId)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Long, ThreadInfoProfile]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def thread(threadId: Long): ThreadInfoProfile =
            mockUnsafeMethod(threadId)
        }

        val a1 = 999L
        val r = mock[ThreadInfoProfile]
        mockUnsafeMethod.expects(a1).returning(r).once()
        grabInfoProfile.tryThread(a1).get should be (r)
      }
    }

    describe("#tryThread(threadReference)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ThreadReference, ThreadInfoProfile]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def thread(threadReference: ThreadReference): ThreadInfoProfile =
            mockUnsafeMethod(threadReference)
        }

        val a1 = mock[ThreadReference]
        val r = mock[ThreadInfoProfile]
        mockUnsafeMethod.expects(a1).returning(r).once()
        grabInfoProfile.tryThread(a1).get should be (r)
      }
    }

    describe("#tryClasses") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[ReferenceTypeInfoProfile]]

        val grabInfoProfile = new TestGrabInfoProfile {
          override def classes: Seq[ReferenceTypeInfoProfile] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[ReferenceTypeInfoProfile])
        mockUnsafeMethod.expects().returning(r).once()
        grabInfoProfile.tryClasses.get should be (r)
      }
    }
  }
}
