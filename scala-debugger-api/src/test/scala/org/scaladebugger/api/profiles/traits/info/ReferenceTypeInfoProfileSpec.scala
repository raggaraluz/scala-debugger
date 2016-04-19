package org.scaladebugger.api.profiles.traits.info

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestReferenceTypeInfoProfile

import scala.util.{Success, Try}

class ReferenceTypeInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  describe("ReferenceTypeInfoProfile") {
    describe("#toPrettyString") {
      it("should display the reference type name") {
        val expected = "some.class.name"

        val referenceTypeInfoProfile = new TestReferenceTypeInfoProfile {
          override def name: String = "some.class.name"
        }

        val actual = referenceTypeInfoProfile.toPrettyString

        actual should be(expected)
      }
    }
  }
}
