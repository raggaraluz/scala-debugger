package org.scaladebugger.api.profiles.traits.info

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestValueInfoProfile

import scala.util.Failure

class ValueInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  describe("ValueInfoProfile") {
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
