package org.scaladebugger.api.profiles.traits.info

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestLocationInfoProfile

import scala.util.{Failure, Success, Try}

class LocationInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  describe("LocationInfoProfile") {
    describe("#toPrettyString") {
      it("should include the source path if available") {
        val expected = "path/to/file.scala : 999"

        val locationInfoProfile = new TestLocationInfoProfile {
          override def tryGetSourcePath: Try[String] =
            Success("path/to/file.scala")
          override def getLineNumber: Int = 999
        }

        val actual = locationInfoProfile.toPrettyString

        actual should be (expected)
      }

      it("should use ??? for the source path if unavailable") {
        val expected = "??? : 999"

        val locationInfoProfile = new TestLocationInfoProfile {
          override def tryGetSourcePath: Try[String] = Failure(new Throwable)
          override def getLineNumber: Int = 999
        }

        val actual = locationInfoProfile.toPrettyString

        actual should be (expected)
      }
    }

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
