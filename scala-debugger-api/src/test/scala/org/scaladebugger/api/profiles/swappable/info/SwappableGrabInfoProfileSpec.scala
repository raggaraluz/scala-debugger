package org.scaladebugger.api.profiles.swappable.info
import acyclic.file

import com.sun.jdi.ThreadReference
import org.scaladebugger.api.profiles.ProfileManager
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfile
import org.scaladebugger.api.profiles.traits.DebugProfile
import org.scaladebugger.api.profiles.traits.info.{ReferenceTypeInfoProfile, ThreadInfoProfile, ValueInfoProfile}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class SwappableGrabInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  private val mockDebugProfile = mock[DebugProfile]
  private val mockProfileManager = mock[ProfileManager]

  private val swappableDebugProfile = new Object with SwappableDebugProfile {
    override protected val profileManager: ProfileManager = mockProfileManager
  }

  describe("SwappableGrabInfoProfile") {
    describe("#getThread") {
      it("should invoke the method on the underlying profile") {
        val expected = mock[ThreadInfoProfile]

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).twice()

        (mockDebugProfile.getThread(_: Long)).expects(*)
          .returning(expected).once()

        (mockDebugProfile.getThread(_: ThreadReference)).expects(*)
          .returning(expected).once()

        swappableDebugProfile.getThread(0L) should be(expected)
        swappableDebugProfile.getThread(mock[ThreadReference]) should be(expected)
      }

      it("should throw an exception if there is no underlying profile") {
        intercept[AssertionError] {
          (mockProfileManager.retrieve _).expects(*).returning(None).once()
          swappableDebugProfile.getThread(0L)
        }

        intercept[AssertionError] {
          (mockProfileManager.retrieve _).expects(*).returning(None).once()
          swappableDebugProfile.getThread(mock[ThreadReference])
        }
      }
    }

    describe("#getClasses") {
      it("should invoke the method on the underlying profile") {
        val expected = Seq(mock[ReferenceTypeInfoProfile])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.getClasses _).expects()
          .returning(expected).once()

        val actual = swappableDebugProfile.getClasses

        actual should be (expected)
      }

      it("should throw an exception if there is no underlying profile") {
        intercept[AssertionError] {
          (mockProfileManager.retrieve _).expects(*).returning(None).once()
          swappableDebugProfile.getClasses
        }
      }
    }
  }
}
