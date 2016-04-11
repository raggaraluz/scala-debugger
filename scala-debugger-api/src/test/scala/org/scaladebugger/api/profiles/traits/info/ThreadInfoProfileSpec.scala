package org.scaladebugger.api.profiles.traits.info

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses
import test.InfoTestClasses.TestThreadInfoProfile

import scala.util.{Success, Failure, Try}

class ThreadInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  describe("ThreadInfoProfile") {
    describe("#findVariableByName") {
      it("should return None if no variable is found") {
        val expected = None
        val name = "someName"

        val threadInfoProfile = new TestThreadInfoProfile {
          // Return 0 frames to test no variable found
          override def getTotalFrames: Int = 0
        }
        val actual = threadInfoProfile.findVariableByName(name)

        actual should be (expected)
      }

      it("should return Some(variable) if found") {
        val expected = Some(mock[VariableInfoProfile])
        val name = "someName"

        val mockFrame = mock[FrameInfoProfile]
        (mockFrame.tryGetVariable _).expects(name)
          .returning(Success(expected.get)).once()

        val threadInfoProfile = new TestThreadInfoProfile {
          override def getFrame(index: Int): FrameInfoProfile = mockFrame
          override def getTotalFrames: Int = 1
        }
        val actual = threadInfoProfile.findVariableByName(name)

        actual should be (expected)
      }
    }

    describe("#tryFindVariableByName") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[String, Option[VariableInfoProfile]]

        val threadInfoProfile = new TestThreadInfoProfile {
          override def findVariableByName(
            name: String
          ): Option[VariableInfoProfile] = mockUnsafeMethod(name)
        }

        val a1 = "someName"
        val r = Some(mock[VariableInfoProfile])
        mockUnsafeMethod.expects(a1).returning(r).once()
        threadInfoProfile.tryFindVariableByName(a1).get should be (r.get)
      }
    }

    describe("#toPrettyString") {
      it("should display the thread name and unique id as a hex code") {
        val expected = "Thread threadName (0xABCDE)"

        val threadInfoProfile = new TestThreadInfoProfile {
          override def uniqueId: Long = Integer.parseInt("ABCDE", 16)
          override def name: String = "threadName"
        }

        val actual = threadInfoProfile.toPrettyString

        actual should be(expected)
      }
    }

    describe("#tryGetFrames") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[FrameInfoProfile]]

        val threadInfoProfile = new TestThreadInfoProfile {
          override def getFrames: Seq[FrameInfoProfile] = mockUnsafeMethod()
        }

        val r = Seq(mock[FrameInfoProfile])
        mockUnsafeMethod.expects().returning(r).once()
        threadInfoProfile.tryGetFrames.get should be (r)
      }
    }

    describe("#tryGetFrame") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Int, FrameInfoProfile]

        val threadInfoProfile = new TestThreadInfoProfile {
          override def getFrame(index: Int): FrameInfoProfile =
            mockUnsafeMethod(index)
        }

        val a1 = 999
        val r = mock[FrameInfoProfile]

        mockUnsafeMethod.expects(a1).returning(r).once()
        threadInfoProfile.tryGetFrame(a1).get should be (r)
      }
    }

    describe("#tryGetTopFrame") {
      it("should invoke withFrame(0) underneath") {
        val mockUnsafeMethod = mockFunction[Int, Try[FrameInfoProfile]]

        val threadInfoProfile = new TestThreadInfoProfile {
          override def tryGetFrame(index: Int): Try[FrameInfoProfile] =
            mockUnsafeMethod(index)
        }

        val r = Success(mock[FrameInfoProfile])

        mockUnsafeMethod.expects(0).returning(r).once()
        threadInfoProfile.tryGetTopFrame should be (r)
      }
    }

    describe("#getTopFrame") {
      it("should invoke withUnsafeFrame(0) underneath") {
        val mockUnsafeMethod = mockFunction[Int, FrameInfoProfile]

        val threadInfoProfile = new TestThreadInfoProfile {
          override def getFrame(index: Int): FrameInfoProfile =
            mockUnsafeMethod(index)
        }

        val r = mock[FrameInfoProfile]

        mockUnsafeMethod.expects(0).returning(r).once()
        threadInfoProfile.getTopFrame should be (r)
      }
    }
  }
}
