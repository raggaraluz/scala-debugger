package org.scaladebugger.api.profiles.traits.info

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestArrayInfoProfile

class ArrayInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  describe("ArrayInfoProfile") {
    describe("#tryGetValue") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Int, ValueInfoProfile]

        val arrayInfoProfile = new TestArrayInfoProfile {
          override def getValue(index: Int): ValueInfoProfile =
            mockUnsafeMethod(index)
        }

        val a1 = 999
        val r = mock[ValueInfoProfile]
        mockUnsafeMethod.expects(a1).returning(r).once()
        arrayInfoProfile.tryGetValue(a1).get should be (r)
      }
    }

    describe("#apply") {
      it("should invoke the unsafe getValue method") {
        val mockUnsafeMethod = mockFunction[Int, ValueInfoProfile]

        val arrayInfoProfile = new TestArrayInfoProfile {
          override def getValue(index: Int): ValueInfoProfile =
            mockUnsafeMethod(index)
        }

        val a1 = 999
        val r = mock[ValueInfoProfile]
        mockUnsafeMethod.expects(a1).returning(r).once()
        arrayInfoProfile(a1) should be (r)
      }
    }

    describe("#trySetValue") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Int, Any, Any]

        val arrayInfoProfile = new TestArrayInfoProfile {
          override def setValue(index: Int, value: Any): Any =
            mockUnsafeMethod(index, value)
        }

        val a1 = 999
        val a2 = "value"
        val r = a2
        mockUnsafeMethod.expects(a1, a2).returning(r).once()
        arrayInfoProfile.trySetValue(a1, a2).get should be (r)
      }
    }

    describe("#update") {
      it("should invoke the unsafe setValue method") {
        val mockUnsafeMethod = mockFunction[Int, Any, Any]

        val arrayInfoProfile = new TestArrayInfoProfile {
          override def setValue(index: Int, value: Any): Any =
            mockUnsafeMethod(index, value)
        }

        val a1 = 999
        val a2 = "value"
        val r = a2
        mockUnsafeMethod.expects(a1, a2).returning(r).once()
        (arrayInfoProfile(a1) = a2) should be (r)
      }
    }

    describe("#tryGetValues(index, totalElements)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Int, Int, Seq[ValueInfoProfile]]

        val arrayInfoProfile = new TestArrayInfoProfile {
          override def getValues(index: Int, totalElements: Int): Seq[ValueInfoProfile] =
            mockUnsafeMethod(index, totalElements)
        }

        val a1 = 999
        val a2 = 40
        val r = Seq(mock[ValueInfoProfile])
        mockUnsafeMethod.expects(a1, a2).returning(r).once()
        arrayInfoProfile.tryGetValues(a1, a2).get should be (r)
      }
    }

    describe("#tryGetValues") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[ValueInfoProfile]]

        val arrayInfoProfile = new TestArrayInfoProfile {
          override def getValues: Seq[ValueInfoProfile] = mockUnsafeMethod()
        }

        val r = Seq(mock[ValueInfoProfile])
        mockUnsafeMethod.expects().returning(r).once()
        arrayInfoProfile.tryGetValues.get should be (r)
      }
    }

    describe("#trySetValues(index, values, srcIndex, totalElements)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Int, Seq[Any], Int, Int, Seq[Any]]

        val arrayInfoProfile = new TestArrayInfoProfile {
          override def setValues(
            index: Int,
            values: Seq[Any],
            srcIndex: Int,
            totalElements: Int
          ): Seq[Any] = mockUnsafeMethod(index, values, srcIndex, totalElements)
        }

        val a1 = 999
        val a2 = Seq(1, 2, "test")
        val a3 = 40
        val a4 = 55
        val r = a2
        mockUnsafeMethod.expects(a1, a2, a3, a4).returning(r).once()
        arrayInfoProfile.trySetValues(a1, a2, a3, a4).get should be (r)
      }
    }

    describe("#trySetValues(values)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[Any], Seq[Any]]

        val arrayInfoProfile = new TestArrayInfoProfile {
          override def setValues(values: Seq[Any]): Seq[Any] = mockUnsafeMethod(values)
        }

        val a1 = Seq(1, 2, "test")
        val r = a1
        mockUnsafeMethod.expects(a1).returning(r).once()
        arrayInfoProfile.trySetValues(a1).get should be (r)
      }
    }
  }
}
