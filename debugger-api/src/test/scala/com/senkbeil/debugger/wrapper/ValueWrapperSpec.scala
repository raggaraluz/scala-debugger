package com.senkbeil.debugger.wrapper

import com.sun.jdi._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfter, Matchers, FunSpec}

class ValueWrapperSpec extends FunSpec with Matchers with BeforeAndAfter
  with MockFactory
{

  private var mockValue: Value = _
  private var valueWrapper: ValueWrapper = _

  before {
    mockValue = mock[Value]
    valueWrapper = new ValueWrapper(mockValue)
  }

  describe("ValueWrapper") {
    describe("#isObject") {
      it("should return true if the value wrapped is an object reference") {
        val valueWrapper = new ValueWrapper(stub[ObjectReference])

        valueWrapper.isObject should be (true)
      }

      it("should return false if the value wrapped is not an object reference") {
        val valueWrapper = new ValueWrapper(stub[PrimitiveValue])

        valueWrapper.isObject should be (false)
      }
    }

    describe("#isPrimitive") {
      it("should return true if the value wrapped is a primitive value") {
        val valueWrapper = new ValueWrapper(stub[PrimitiveValue])

        valueWrapper.isPrimitive should be (true)
      }

      it("should return false if the value wrapped is not a primitive value") {
        val valueWrapper = new ValueWrapper(stub[ObjectReference])

        valueWrapper.isPrimitive should be (false)
      }
    }

    describe("#value") {
      it("should return a boolean if wrapping a BooleanValue") {
        val expected: Boolean = true

        // TODO: Investigate creating a real value without needing a
        //       VirtualMachine instance
        // Set up the value to return
        val value = stub[BooleanValue]
        (value.value _).when().returns(expected)

        val actual = new ValueWrapper(value).value().asInstanceOf[Boolean]

        actual should be (expected)
      }

      it("should return a byte if wrapping a ByteValue") {
        val expected: Byte = 3.toByte

        // TODO: Investigate creating a real value without needing a
        //       VirtualMachine instance
        // Set up the value to return
        val value = stub[ByteValue]
        (value.value _).when().returns(expected)

        val actual = new ValueWrapper(value).value().asInstanceOf[Byte]

        actual should be (expected)
      }

      it("should return a char if wrapping a CharValue") {
        val expected: Char = 'a'

        // TODO: Investigate creating a real value without needing a
        //       VirtualMachine instance
        // Set up the value to return
        val value = stub[CharValue]
        (value.value _).when().returns(expected)

        val actual = new ValueWrapper(value).value().asInstanceOf[Char]

        actual should be (expected)
      }

      it("should return a double if wrapping a DoubleValue") {
        val expected: Double = 3.0

        // TODO: Investigate creating a real value without needing a
        //       VirtualMachine instance
        // Set up the value to return
        val value = stub[DoubleValue]
        (value.value _).when().returns(expected)

        val actual = new ValueWrapper(value).value().asInstanceOf[Double]

        actual should be (expected)
      }

      it("should return a float if wrapping a FloatValue") {
        val expected: Float = 3.0f

        // TODO: Investigate creating a real value without needing a
        //       VirtualMachine instance
        // Set up the value to return
        val value = stub[FloatValue]
        (value.value _).when().returns(expected)

        val actual = new ValueWrapper(value).value().asInstanceOf[Float]

        actual should be (expected)
      }

      it("should return an integer if wrapping an IntegerValue") {
        val expected: Int = 3

        // TODO: Investigate creating a real value without needing a
        //       VirtualMachine instance
        // Set up the value to return
        val value = stub[IntegerValue]
        (value.value _).when().returns(expected)

        val actual = new ValueWrapper(value).value().asInstanceOf[Int]

        actual should be (expected)
      }

      it("should return a long if wrapping a LongValue") {
        val expected: Long = 3

        // TODO: Investigate creating a real value without needing a
        //       VirtualMachine instance
        // Set up the value to return
        val value = stub[LongValue]
        (value.value _).when().returns(expected)

        val actual = new ValueWrapper(value).value().asInstanceOf[Long]

        actual should be (expected)
      }

      it("should return a short if wrapping a ShortValue") {
        val expected: Short = 3

        // TODO: Investigate creating a real value without needing a
        //       VirtualMachine instance
        // Set up the value to return
        val value = stub[ShortValue]
        (value.value _).when().returns(expected)

        val actual = new ValueWrapper(value).value().asInstanceOf[Short]

        actual should be (expected)
      }

      it("should throw an exception if not a primitive") {
        intercept[IllegalArgumentException] {
          // TODO: Investigate creating a real value without needing a
          //       VirtualMachine instance
          new ValueWrapper(stub[ObjectReference]).value()
        }
      }

      it("should throw an exception if given an unexpected primitive") {
        intercept[RuntimeException] {
          // TODO: Investigate creating a real value without needing a
          //       VirtualMachine instance
          // Represents an unknown primitive value that we are not checking
          new ValueWrapper(stub[PrimitiveValue]).value()
        }
      }
    }

    describe("#fieldsAndValues") {

    }

    describe("#toString") {

    }
  }
}
