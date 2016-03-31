package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.ValueInfoProfile
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class PureFieldInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockObjectReference = mock[ObjectReference]
  private val mockField = mock[Field]
  private val pureFieldInfoProfile = new PureFieldInfoProfile(
    mockObjectReference,
    mockField
  )(mockVirtualMachine)

  describe("PureFieldInfoProfile") {
    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockField

        val actual = pureFieldInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#name") {
      it("should return the field's name") {
        val expected = "someName"

        (mockField.name _).expects().returning(expected).once()

        val actual = pureFieldInfoProfile.name

        actual should be (expected)
      }
    }

    describe("#isField") {
      it("should return true") {
        val expected = true

        val actual = pureFieldInfoProfile.isField

        actual should be (expected)
      }
    }

    describe("#isArgument") {
      it("should return false") {
        val expected = false

        val actual = pureFieldInfoProfile.isArgument

        actual should be (expected)
      }
    }

    describe("#isLocal") {
      it("should return false") {
        val expected = false

        val actual = pureFieldInfoProfile.isLocal

        actual should be (expected)
      }
    }

    describe("#setValue") {
      it("should set strings directly on the object") {
        val expected = "some value"

        // Mirror the local string remotely
        val mockStringReference = mock[StringReference]
        (mockVirtualMachine.mirrorOf(_: String)).expects(expected)
          .returning(mockStringReference).once()

        // Ensure setting the value on the object is verified
        (mockObjectReference.setValue _)
          .expects(mockField, mockStringReference)
          .once()

        pureFieldInfoProfile.setValue(expected) should be (expected)
      }

      it("should set primitive values directly on the object") {
        val expected = 3.toByte

        // Mirror the local byte remotely
        val mockByteValue = mock[ByteValue]
        (mockVirtualMachine.mirrorOf(_: Byte)).expects(expected)
          .returning(mockByteValue).once()

        // Ensure setting the value on the object is verified
        (mockObjectReference.setValue _)
          .expects(mockField, mockByteValue)
          .once()

        pureFieldInfoProfile.setValue(expected) should be (expected)
      }
    }

    describe("#toValue") {
      it("should return a wrapper around the value of the field") {
        val expected = mock[ValueInfoProfile]
        val mockValue = mock[Value]

        // Retrieving the value of the field returns our mock
        (mockObjectReference.getValue _).expects(mockField)
          .returning(mockValue).once()

        val mockNewValueProfile = mockFunction[Value, ValueInfoProfile]
        val pureFieldInfoProfile = new PureFieldInfoProfile(
          mockObjectReference,
          mockField
        )(mockVirtualMachine) {
          override protected def newValueProfile(value: Value): ValueInfoProfile =
            mockNewValueProfile(value)
        }

        mockNewValueProfile.expects(mockValue).returning(expected).once()
        pureFieldInfoProfile.toValue should be (expected)
      }
    }
  }
}
