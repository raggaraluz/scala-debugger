package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.{ArrayInfoProfile, ObjectInfoProfile, PrimitiveInfoProfile}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class PurePrimitiveInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  private val mockPrimitiveValue = mock[PrimitiveValue]

  describe("PurePrimitiveInfoProfile") {
    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockPrimitiveValue

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mockPrimitiveValue
        )

        val actual = purePrimitiveInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#isBoolean") {
      it("should return true if the value is a boolean") {
        val expected = true

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mock[BooleanValue]
        )

        val actual = purePrimitiveInfoProfile.isBoolean

        actual should be (expected)
      }

      it("should return false if the value is not a boolean") {
        val expected = false

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mock[PrimitiveValue]
        )

        val actual = purePrimitiveInfoProfile.isBoolean

        actual should be (expected)
      }
    }

    describe("#isByte") {
      it("should return true if the value is a byte") {
        val expected = true

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mock[ByteValue]
        )

        val actual = purePrimitiveInfoProfile.isByte

        actual should be (expected)
      }

      it("should return false if the value is not a byte") {
        val expected = false

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mock[PrimitiveValue]
        )

        val actual = purePrimitiveInfoProfile.isByte

        actual should be (expected)
      }
    }

    describe("#isChar") {
      it("should return true if the value is a char") {
        val expected = true

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mock[CharValue]
        )

        val actual = purePrimitiveInfoProfile.isChar

        actual should be (expected)
      }

      it("should return false if the value is not a char") {
        val expected = false

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mock[PrimitiveValue]
        )

        val actual = purePrimitiveInfoProfile.isChar

        actual should be (expected)
      }
    }

    describe("#isInteger") {
      it("should return true if the value is an integer") {
        val expected = true

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mock[IntegerValue]
        )

        val actual = purePrimitiveInfoProfile.isInteger

        actual should be (expected)
      }

      it("should return false if the value is not an integer") {
        val expected = false

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mock[PrimitiveValue]
        )

        val actual = purePrimitiveInfoProfile.isInteger

        actual should be (expected)
      }
    }

    describe("#isLong") {
      it("should return true if the value is a long") {
        val expected = true

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mock[LongValue]
        )

        val actual = purePrimitiveInfoProfile.isLong

        actual should be (expected)
      }

      it("should return false if the value is not a long") {
        val expected = false

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mock[PrimitiveValue]
        )

        val actual = purePrimitiveInfoProfile.isLong

        actual should be (expected)
      }
    }

    describe("#isShort") {
      it("should return true if the value is a short") {
        val expected = true

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mock[ShortValue]
        )

        val actual = purePrimitiveInfoProfile.isShort

        actual should be (expected)
      }

      it("should return false if the value is not a short") {
        val expected = false

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mock[PrimitiveValue]
        )

        val actual = purePrimitiveInfoProfile.isShort

        actual should be (expected)
      }
    }

    describe("#isDouble") {
      it("should return true if the value is a double") {
        val expected = true

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mock[DoubleValue]
        )

        val actual = purePrimitiveInfoProfile.isDouble

        actual should be (expected)
      }

      it("should return false if the value is not a double") {
        val expected = false

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mock[PrimitiveValue]
        )

        val actual = purePrimitiveInfoProfile.isDouble

        actual should be (expected)
      }
    }

    describe("#isFloat") {
      it("should return true if the value is a float") {
        val expected = true

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mock[FloatValue]
        )

        val actual = purePrimitiveInfoProfile.isFloat

        actual should be (expected)
      }

      it("should return false if the value is not a float") {
        val expected = false

        val purePrimitiveInfoProfile = new PurePrimitiveInfoProfile(
          mock[PrimitiveValue]
        )

        val actual = purePrimitiveInfoProfile.isFloat

        actual should be (expected)
      }
    }
  }
}
