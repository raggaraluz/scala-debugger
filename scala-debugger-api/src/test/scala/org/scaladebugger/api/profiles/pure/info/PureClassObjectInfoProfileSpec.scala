package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.lowlevel.{InvokeNonVirtualArgument, InvokeSingleThreadedArgument, JDIArgument}
import org.scaladebugger.api.profiles.traits.info._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class PureClassObjectInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  private val mockNewReferenceTypeProfile = mockFunction[ReferenceType, ReferenceTypeInfoProfile]
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockReferenceType = mock[ReferenceType]
  private val mockThreadReference = mock[ThreadReference]
  private val mockClassObjectReference = mock[ClassObjectReference]
  private val pureClassObjectInfoProfile = new PureClassObjectInfoProfile(
    mockClassObjectReference
  )(
    threadReference = mockThreadReference,
    virtualMachine = mockVirtualMachine,
    referenceType = mockReferenceType
  ) {
    override protected def newReferenceTypeProfile(
      referenceType: ReferenceType
    ): ReferenceTypeInfoProfile = mockNewReferenceTypeProfile(referenceType)
  }

  describe("PureClassObjectInfoProfile") {
    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockClassObjectReference

        val actual = pureClassObjectInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#getReflectedType") {
      it("should return a profile wrapper around the reference type represented by the class") {
        val expected = mock[ReferenceTypeInfoProfile]
        val referenceType = mock[ReferenceType]

        (mockClassObjectReference.reflectedType _).expects()
          .returning(referenceType).once()

        mockNewReferenceTypeProfile.expects(referenceType)
          .returning(expected).once()

        val actual = pureClassObjectInfoProfile.getReflectedType

        actual should be (expected)
      }
    }
  }
}
