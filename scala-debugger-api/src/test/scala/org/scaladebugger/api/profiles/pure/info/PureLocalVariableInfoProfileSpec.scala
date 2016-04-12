package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.{FrameInfoProfile, ValueInfoProfile}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class PureLocalVariableInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockFrameInfoProfile = mock[FrameInfoProfile]
  private val mockLocalVariable = mock[LocalVariable]
  private val TestOffsetIndex = 999
  private val pureLocalVariableInfoProfile = new PureLocalVariableInfoProfile(
    mockFrameInfoProfile,
    mockLocalVariable,
    TestOffsetIndex
  )(mockVirtualMachine)

  describe("PureLocalVariableInfoProfile") {
    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockLocalVariable

        val actual = pureLocalVariableInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#name") {
      it("should return the local variable's name") {
        val expected = "someName"

        (mockLocalVariable.name _).expects().returning(expected).once()

        val actual = pureLocalVariableInfoProfile.name

        actual should be (expected)
      }
    }

    describe("#isLocal") {
      it("should return true") {
        val expected = true

        val actual = pureLocalVariableInfoProfile.isLocal

        actual should be (expected)
      }
    }

    describe("#isArgument") {
      it("should return true if is an argument") {
        val expected = true

        (mockLocalVariable.isArgument _).expects().returning(true).once()

        val actual = pureLocalVariableInfoProfile.isArgument

        actual should be (expected)
      }

      it("should return false if is not an argument") {
        val expected = false

        (mockLocalVariable.isArgument _).expects().returning(false).once()

        val actual = pureLocalVariableInfoProfile.isArgument

        actual should be (expected)
      }
    }

    describe("#isField") {
      it("should return false") {
        val expected = false

        val actual = pureLocalVariableInfoProfile.isField

        actual should be (expected)
      }
    }

    describe("#trySetValue") {
      it("should set strings directly on the object") {
        val expected = "some value"

        // Mirror the local string remotely
        val mockStringReference = mock[StringReference]
        (mockVirtualMachine.mirrorOf(_: String)).expects(expected)
          .returning(mockStringReference).once()

        // Retrieve the underlying stack frame
        val mockStackFrame = mock[StackFrame]
        (mockFrameInfoProfile.toJdiInstance _).expects()
          .returning(mockStackFrame).once()

        // Ensure setting value on stack frame is verified
        (mockStackFrame.setValue _)
          .expects(mockLocalVariable, mockStringReference)
          .once()

        pureLocalVariableInfoProfile.trySetValue(expected).get should be (expected)
      }

      it("should set primitive values directly on the object") {
        val expected = 3.toByte

        // Mirror the local string remotely
        val mockByteValue = mock[ByteValue]
        (mockVirtualMachine.mirrorOf(_: Byte)).expects(expected)
          .returning(mockByteValue).once()

        // Retrieve the underlying stack frame
        val mockStackFrame = mock[StackFrame]
        (mockFrameInfoProfile.toJdiInstance _).expects()
          .returning(mockStackFrame).once()

        // Ensure setting value on stack frame is verified
        (mockStackFrame.setValue _)
          .expects(mockLocalVariable, mockByteValue)
          .once()

        pureLocalVariableInfoProfile.trySetValue(expected).get should be (expected)
      }
    }

    describe("#toValue") {
      it("should return a wrapper around the value of the localVariable") {
        val expected = mock[ValueInfoProfile]
        val mockValue = mock[Value]

        // Retrieve the underlying stack frame
        val mockStackFrame = mock[StackFrame]
        (mockFrameInfoProfile.toJdiInstance _).expects()
          .returning(mockStackFrame).once()

        // Retrieve the value from the stack frame
        (mockStackFrame.getValue _).expects(mockLocalVariable)
          .returning(mockValue).once()

        val mockNewValueProfile = mockFunction[Value, ValueInfoProfile]
        val pureLocalVariableInfoProfile = new PureLocalVariableInfoProfile(
          mockFrameInfoProfile,
          mockLocalVariable,
          TestOffsetIndex
        )(mockVirtualMachine) {
          override protected def newValueProfile(value: Value): ValueInfoProfile =
            mockNewValueProfile(value)
        }

        mockNewValueProfile.expects(mockValue).returning(expected).once()
        pureLocalVariableInfoProfile.toValue should be (expected)
      }
    }
  }
}
