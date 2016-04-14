package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class PureClassLoaderInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  private val mockNewReferenceTypeProfile = mockFunction[ReferenceType, ReferenceTypeInfoProfile]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockReferenceType = mock[ReferenceType]
  private val mockThreadReference = mock[ThreadReference]
  private val mockClassLoaderReference = mock[ClassLoaderReference]
  private val pureClassLoaderInfoProfile = new PureClassLoaderInfoProfile(
    mockScalaVirtualMachine, mockClassLoaderReference
  )(
    threadReference = mockThreadReference,
    virtualMachine = mockVirtualMachine,
    referenceType = mockReferenceType
  ) {
    override protected def newReferenceTypeProfile(
      referenceType: ReferenceType
    ): ReferenceTypeInfoProfile = mockNewReferenceTypeProfile(referenceType)
  }

  describe("PureClassLoaderInfoProfile") {
    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockClassLoaderReference

        val actual = pureClassLoaderInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#getDefinedClasses") {
      it("should return a collection of profile wrappers for reference types of defined classes") {
        val expected = Seq(mock[ReferenceTypeInfoProfile])
        val referenceTypes = Seq(mock[ReferenceType])

        import scala.collection.JavaConverters._
        (mockClassLoaderReference.definedClasses _).expects()
          .returning(referenceTypes.asJava).once()

        expected.zip(referenceTypes).foreach { case (e, r) =>
          mockNewReferenceTypeProfile.expects(r).returning(e).once()
        }

        val actual = pureClassLoaderInfoProfile.getDefinedClasses

        actual should be (expected)
      }
    }

    describe("#getVisibleClasses") {
      it("should return a collection of profile wrappers for reference types of visible classes") {
        val expected = Seq(mock[ReferenceTypeInfoProfile])
        val referenceTypes = Seq(mock[ReferenceType])

        import scala.collection.JavaConverters._
        (mockClassLoaderReference.visibleClasses _).expects()
          .returning(referenceTypes.asJava).once()

        expected.zip(referenceTypes).foreach { case (e, r) =>
          mockNewReferenceTypeProfile.expects(r).returning(e).once()
        }

        val actual = pureClassLoaderInfoProfile.getVisibleClasses

        actual should be (expected)
      }
    }
  }
}
