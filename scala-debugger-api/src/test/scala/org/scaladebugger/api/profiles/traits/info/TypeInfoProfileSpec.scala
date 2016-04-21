package org.scaladebugger.api.profiles.traits.info

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.{TestArrayInfoProfile, TestTypeInfoProfile}

import scala.util.{Failure, Success, Try}

class TypeInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  describe("TypeInfoProfile") {
    describe("#toPrettyString") {
      it("should include the type name and signature") {
        val expected = "Type NAME (SIGNATURE)"

        val typeInfoProfile = new TestTypeInfoProfile {
          override def name: String = "NAME"
          override def signature: String = "SIGNATURE"
        }

        val actual = typeInfoProfile.toPrettyString

        actual should be (expected)
      }
    }
  }
}
