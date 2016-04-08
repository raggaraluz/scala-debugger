package org.scaladebugger.api.profiles.traits.info

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestValueInfoProfile

import scala.util.Failure

class ValueInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  describe("ValueInfoProfile") {
    describe("#toPrettyString") {
      it("should display null if the value is null") {
        val expected = "null"

        val valueInfoProfile = new TestValueInfoProfile {
          override def isPrimitive: Boolean = false
          override def isObject: Boolean = false
          override def isString: Boolean = false
          override def isArray: Boolean = false
          override def isVoid: Boolean = false
          override def isNull: Boolean = true
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display void if the value is void") {
        val expected = "void"

        val valueInfoProfile = new TestValueInfoProfile {
          override def isPrimitive: Boolean = false
          override def isObject: Boolean = false
          override def isString: Boolean = false
          override def isArray: Boolean = false
          override def isVoid: Boolean = true
          override def isNull: Boolean = false
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display the value in quotes if the value is a string") {
        val expected = "\"some value\""

        val valueInfoProfile = new TestValueInfoProfile {
          override def toLocalValue: Any = "some value"
          override def isPrimitive: Boolean = false
          override def isObject: Boolean = true
          override def isString: Boolean = true
          override def isArray: Boolean = false
          override def isVoid: Boolean = false
          override def isNull: Boolean = false
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display the pretty string of the array instance if an array") {
        val expected = "ARRAY"

        val mockArrayInfoProfile = mock[ArrayInfoProfile]
        (mockArrayInfoProfile.toPrettyString _).expects()
          .returning(expected).once()

        val valueInfoProfile = new TestValueInfoProfile {
          override def isPrimitive: Boolean = false
          override def isObject: Boolean = true
          override def isString: Boolean = false
          override def isArray: Boolean = true
          override def isVoid: Boolean = false
          override def isNull: Boolean = false
          override def toArray: ArrayInfoProfile = mockArrayInfoProfile
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display the pretty string of the object instance if an object") {
        val expected = "OBJECT"

        val mockObjectInfoProfile = mock[ObjectInfoProfile]
        (mockObjectInfoProfile.toPrettyString _).expects()
          .returning(expected).once()

        val valueInfoProfile = new TestValueInfoProfile {
          override def isPrimitive: Boolean = false
          override def isObject: Boolean = true
          override def isString: Boolean = false
          override def isArray: Boolean = false
          override def isVoid: Boolean = false
          override def isNull: Boolean = false
          override def toObject: ObjectInfoProfile = mockObjectInfoProfile
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display the pretty string of the primitive instance if a primitive") {
        val expected = "ARRAY"

        val mockPrimitiveInfoProfile = mock[PrimitiveInfoProfile]
        (mockPrimitiveInfoProfile.toPrettyString _).expects()
          .returning(expected).once()

        val valueInfoProfile = new TestValueInfoProfile {
          override def isPrimitive: Boolean = true
          override def isObject: Boolean = false
          override def isString: Boolean = false
          override def isArray: Boolean = false
          override def isVoid: Boolean = false
          override def isNull: Boolean = false
          override def toPrimitive: PrimitiveInfoProfile = mockPrimitiveInfoProfile
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display ??? if unrecognized value") {
        val expected = "???"

        val valueInfoProfile = new TestValueInfoProfile {
          override def isPrimitive: Boolean = false
          override def isObject: Boolean = false
          override def isString: Boolean = false
          override def isArray: Boolean = false
          override def isVoid: Boolean = false
          override def isNull: Boolean = false
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display <ERROR> if failed to retrieve value") {
        val expected = "<ERROR>"

        val valueInfoProfile = new TestValueInfoProfile {
          override def toLocalValue: Any = throw new Throwable
          override def isPrimitive: Boolean = false
          override def isObject: Boolean = false
          override def isString: Boolean = true
          override def isArray: Boolean = false
          override def isVoid: Boolean = false
          override def isNull: Boolean = false
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }
    }

    describe("#tryToPrimitive") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[PrimitiveInfoProfile]

        val valueInfoProfile = new TestValueInfoProfile {
          override def toPrimitive: PrimitiveInfoProfile = mockUnsafeMethod()
        }

        val r = mock[PrimitiveInfoProfile]
        mockUnsafeMethod.expects().returning(r).once()
        valueInfoProfile.tryToPrimitive.get should be (r)
      }
    }

    describe("#tryToObject") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ObjectInfoProfile]

        val valueInfoProfile = new TestValueInfoProfile {
          override def toObject: ObjectInfoProfile = mockUnsafeMethod()
        }

        val r = mock[ObjectInfoProfile]
        mockUnsafeMethod.expects().returning(r).once()
        valueInfoProfile.tryToObject.get should be (r)
      }
    }

    describe("#tryToLocalValue") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Any]

        val valueInfoProfile = new TestValueInfoProfile {
          override def toLocalValue: Any = mockUnsafeMethod()
        }

        val r = 3
        mockUnsafeMethod.expects().returning(r).once()
        valueInfoProfile.tryToLocalValue.get should be (r)
      }
    }

    describe("#tryToArray") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ArrayInfoProfile]

        val valueInfoProfile = new TestValueInfoProfile {
          override def toArray: ArrayInfoProfile = mockUnsafeMethod()
        }

        val r = mock[ArrayInfoProfile]
        mockUnsafeMethod.expects().returning(r).once()
        valueInfoProfile.tryToArray.get should be (r)
      }
    }
  }
}
