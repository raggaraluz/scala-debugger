package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi.{ReferenceType, StackFrame, ThreadReference, VirtualMachine}
import org.scaladebugger.api.profiles.traits.info.FrameInfoProfile
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class PureThreadInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockReferenceType = mock[ReferenceType]
  private val mockNewFrameProfile = mockFunction[StackFrame, Int, FrameInfoProfile]
  private val mockThreadReference = mock[ThreadReference]
  private val pureThreadInfoProfile = new PureThreadInfoProfile(
    mockScalaVirtualMachine,
    mockThreadReference
  )(
    virtualMachine = mockVirtualMachine,
    referenceType = mockReferenceType
  ) {
    override protected def newFrameProfile(
      stackFrame: StackFrame,
      index: Int
    ): FrameInfoProfile = mockNewFrameProfile(stackFrame, index)
  }

  describe("PureThreadInfoProfile") {
    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockThreadReference

        val actual = pureThreadInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#name") {
      it("should return the name of the thread") {
        val expected = "some name"

        (mockThreadReference.name _).expects().returning(expected).once()

        val actual = pureThreadInfoProfile.name

        actual should be (expected)
      }
    }

    describe("#getFrames") {
      it("should return a collection of profiles wrapping the frames of the suspended thread") {
        val expected = Seq(mock[FrameInfoProfile])

        val mockStackFrames = Seq(mock[StackFrame])

        // Retrieve raw stack frame
        import scala.collection.JavaConverters._
        (mockThreadReference.frames: Function0[java.util.List[StackFrame]])
          .expects()
          .returning(mockStackFrames.asJava)
          .once()

        // Wrap stack frame in profile instance
        mockStackFrames.zip(expected).zipWithIndex.foreach { case ((sf, e), i) =>
          mockNewFrameProfile.expects(sf, i).returning(e).once()
        }

        val actual = pureThreadInfoProfile.getFrames

        actual should be (expected)
      }
    }

    describe("#getFrame") {
      it("should return a profile wrapping the frame at the specified position in the stack") {
        val expected = mock[FrameInfoProfile]

        val index = 999
        val mockStackFrame = mock[StackFrame]

        // Retrieve raw stack frame
        (mockThreadReference.frame _).expects(index)
          .returning(mockStackFrame).once()

        // Wrap stack frame in profile instance
        mockNewFrameProfile.expects(mockStackFrame, index)
          .returning(expected).once()

        val actual = pureThreadInfoProfile.getFrame(index)

        actual should be (expected)
      }
    }

    describe("#getTotalFrames") {
      it("should return the total number of frames available in the stack of the suspended thread") {
        val expected = 999

        (mockThreadReference.frameCount _).expects().returning(expected).once()

        val actual = pureThreadInfoProfile.getTotalFrames

        actual should be (expected)
      }
    }
  }
}
