package org.scaladebugger.api.profiles.traits.info

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestLocationInfoProfile

class LocationInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  describe("LocationInfoProfile") {
    describe("#getCodeIndexOption") {
      it("should return Some(code position) if position is 0 or greater") {
        val expected = Some(999)

        val locationInfoProfile = new TestLocationInfoProfile {
          override def getCodeIndex: Long = expected.get
        }

        val actual = locationInfoProfile.getCodeIndexOption

        actual should be (expected)
      }

      it("should return None if position is less than 0") {
        val expected = None

        val locationInfoProfile = new TestLocationInfoProfile {
          override def getCodeIndex: Long = -1
        }

        val actual = locationInfoProfile.getCodeIndexOption

        actual should be (expected)
      }
    }

    describe("#getLineNumberOption") {
      it("should return Some(line number) if line is 0 or greater") {
        val expected = Some(999)

        val locationInfoProfile = new TestLocationInfoProfile {
          override def getLineNumber: Int = expected.get
        }

        val actual = locationInfoProfile.getLineNumberOption

        actual should be (expected)
      }

      it("should return None if line is less than 0") {
        val expected = None

        val locationInfoProfile = new TestLocationInfoProfile {
          override def getLineNumber: Int = -1
        }

        val actual = locationInfoProfile.getLineNumberOption

        actual should be (expected)
      }
    }
  }
}
