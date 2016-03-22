package org.scaladebugger.api.profiles.traits.info

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestMethodInfoProfile

class MethodInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  describe("MethodInfoProfile") {
    describe("#tryGetReturnTypeName") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[String]

        val methodInfoProfile = new TestMethodInfoProfile {
          override def getReturnTypeName: String = mockUnsafeMethod()
        }

        val r = "some.return.type"
        mockUnsafeMethod.expects().returning(r).once()
        methodInfoProfile.tryGetReturnTypeName.get should be (r)
      }
    }

    describe("#tryGetParameterTypeNames") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[String]]

        val methodInfoProfile = new TestMethodInfoProfile {
          override def getParameterTypeNames: Seq[String] = mockUnsafeMethod()
        }

        val r = Seq("some.param.type")
        mockUnsafeMethod.expects().returning(r).once()
        methodInfoProfile.tryGetParameterTypeNames.get should be (r)
      }
    }
  }
}
