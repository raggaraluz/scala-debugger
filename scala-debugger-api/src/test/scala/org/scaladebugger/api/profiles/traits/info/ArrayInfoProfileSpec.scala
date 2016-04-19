package org.scaladebugger.api.profiles.traits.info

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestArrayInfoProfile

import scala.util.{Failure, Success, Try}

class ArrayInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  describe("ArrayInfoProfile") {
    describe("#toPrettyString") {
      it("should support empty arrays") {
        val expected = "Array(length = 0)[<EMPTY>]"

        val index = 0
        val maxLength = 10
        val r: Try[Seq[ValueInfoProfile]] = Success(Nil)

        val mockUnsafeMethod = mockFunction[Int, Int, Try[Seq[ValueInfoProfile]]]
        mockUnsafeMethod.expects(index, maxLength).returning(r).once()

        val arrayInfoProfile = new TestArrayInfoProfile {
          override def length: Int = r.get.length

          override def tryValues(
            index: Int,
            totalElements: Int
          ): Try[Seq[ValueInfoProfile]] = mockUnsafeMethod(index, totalElements)
        }

        val actual = arrayInfoProfile.toPrettyString(maxLength)

        actual should be (expected)
      }

      it("should support arrays with length less than or equal to the maximum") {
        val expected = "Array(length = 2)[test1,test2]"

        val index = 0
        val maxLength = 2
        val r = Success((1 to maxLength).map(_ => mock[ValueInfoProfile]))
        r.get.zipWithIndex.foreach { case (v, i) =>
          (v.toPrettyString _).expects().returning(s"test${i+1}").once()
        }

        val mockUnsafeMethod = mockFunction[Int, Int, Try[Seq[ValueInfoProfile]]]
        mockUnsafeMethod.expects(index, maxLength).returning(r).once()

        val arrayInfoProfile = new TestArrayInfoProfile {
          override def length: Int = r.get.length

          override def tryValues(
            index: Int,
            totalElements: Int
          ): Try[Seq[ValueInfoProfile]] = mockUnsafeMethod(index, totalElements)
        }

        val actual = arrayInfoProfile.toPrettyString(maxLength)

        actual should be (expected)
      }

      it("should cut off additional values from arrays with length over the maximum") {
        val expected = "Array(length = 3)[test1,test2,...]"

        val index = 0
        val maxLength = 2
        val r = Success((1 to maxLength).map(_ => mock[ValueInfoProfile]))
        r.get.zipWithIndex.foreach { case (v, i) =>
          (v.toPrettyString _).expects().returning(s"test${i+1}").once()
        }

        val mockUnsafeMethod = mockFunction[Int, Int, Try[Seq[ValueInfoProfile]]]
        mockUnsafeMethod.expects(index, maxLength).returning(r).once()

        val arrayInfoProfile = new TestArrayInfoProfile {
          override def length: Int = r.get.length + 1

          override def tryValues(
            index: Int,
            totalElements: Int
          ): Try[Seq[ValueInfoProfile]] = mockUnsafeMethod(index, totalElements)
        }

        val actual = arrayInfoProfile.toPrettyString(maxLength)

        actual should be (expected)
      }

      it("should return no values if failing to get array values") {
        val expected = "Array(length = 3)[<ERROR>]"

        val index = 0
        val maxLength = 3
        val r = Failure(new Throwable)

        val mockUnsafeMethod = mockFunction[Int, Int, Try[Seq[ValueInfoProfile]]]
        mockUnsafeMethod.expects(index, maxLength).returning(r).once()

        val arrayInfoProfile = new TestArrayInfoProfile {
          override def length: Int = maxLength

          override def tryValues(
            index: Int,
            totalElements: Int
          ): Try[Seq[ValueInfoProfile]] = mockUnsafeMethod(index, totalElements)
        }

        val actual = arrayInfoProfile.toPrettyString(maxLength)

        actual should be (expected)
      }
    }

    describe("#tryValue") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Int, ValueInfoProfile]

        val arrayInfoProfile = new TestArrayInfoProfile {
          override def value(index: Int): ValueInfoProfile =
            mockUnsafeMethod(index)
        }

        val a1 = 999
        val r = mock[ValueInfoProfile]
        mockUnsafeMethod.expects(a1).returning(r).once()
        arrayInfoProfile.tryValue(a1).get should be (r)
      }
    }

    describe("#apply") {
      it("should invoke the unsafe value method") {
        val mockUnsafeMethod = mockFunction[Int, ValueInfoProfile]

        val arrayInfoProfile = new TestArrayInfoProfile {
          override def value(index: Int): ValueInfoProfile =
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

    describe("#tryValues(index, totalElements)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Int, Int, Seq[ValueInfoProfile]]

        val arrayInfoProfile = new TestArrayInfoProfile {
          override def values(index: Int, totalElements: Int): Seq[ValueInfoProfile] =
            mockUnsafeMethod(index, totalElements)
        }

        val a1 = 999
        val a2 = 40
        val r = Seq(mock[ValueInfoProfile])
        mockUnsafeMethod.expects(a1, a2).returning(r).once()
        arrayInfoProfile.tryValues(a1, a2).get should be (r)
      }
    }

    describe("#tryValues") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[ValueInfoProfile]]

        val arrayInfoProfile = new TestArrayInfoProfile {
          override def values: Seq[ValueInfoProfile] = mockUnsafeMethod()
        }

        val r = Seq(mock[ValueInfoProfile])
        mockUnsafeMethod.expects().returning(r).once()
        arrayInfoProfile.tryValues.get should be (r)
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
