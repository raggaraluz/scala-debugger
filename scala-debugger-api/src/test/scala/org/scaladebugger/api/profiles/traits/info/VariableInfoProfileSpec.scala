package org.scaladebugger.api.profiles.traits.info

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.{TestValueInfoProfile, TestVariableInfoProfile}

import scala.util.{Failure, Success, Try}

class VariableInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  describe("VariableInfoProfile") {
    describe("#toPrettyString") {
      it("should display the variable name and pretty-stringed value") {
        val expected = "someName = someValue"

        val mockValueInfoProfile = mock[ValueInfoProfile]
        (mockValueInfoProfile.toPrettyString _).expects()
          .returning("someValue").once()

        val variableInfoProfile = new TestVariableInfoProfile {
          override def name: String = "someName"
          override def tryToValue: Try[ValueInfoProfile] =
            Success(mockValueInfoProfile)
        }

        val actual = variableInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display ??? for the value if unavailable") {
        val expected = "someName = ???"

        val variableInfoProfile = new TestVariableInfoProfile {
          override def name: String = "someName"
          override def tryToValue: Try[ValueInfoProfile] =
            Failure(new Throwable)
        }

        val actual = variableInfoProfile.toPrettyString

        actual should be(expected)
      }
    }

    describe("#toValue") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ValueInfoProfile]

        val variableInfoProfile = new TestVariableInfoProfile {
          override def toValue: ValueInfoProfile = mockUnsafeMethod()
        }

        val r = mock[ValueInfoProfile]
        mockUnsafeMethod.expects().returning(r).once()
        variableInfoProfile.tryToValue.get should be (r)
      }
    }
  }
}
