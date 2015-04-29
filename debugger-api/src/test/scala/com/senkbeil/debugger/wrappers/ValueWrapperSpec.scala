package com.senkbeil.debugger.wrappers

import com.sun.jdi._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, FunSpec}
import test.JDIMockHelpers

class ValueWrapperSpec extends FunSpec with Matchers with MockFactory
  with JDIMockHelpers
{
  describe("ValueWrapper") {
    describe("constructor") {
      it("should throw an exception if wrapping a null pointer") {
        intercept[IllegalArgumentException] {
          new ValueWrapper(null)
        }
      }
    }

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

        val value = createPrimitiveValueStub(expected)

        val actual = new ValueWrapper(value).value().asInstanceOf[Boolean]

        actual should be (expected)
      }

      it("should return a byte if wrapping a ByteValue") {
        val expected: Byte = 3.toByte

        val value = createPrimitiveValueStub(expected)

        val actual = new ValueWrapper(value).value().asInstanceOf[Byte]

        actual should be (expected)
      }

      it("should return a char if wrapping a CharValue") {
        val expected: Char = 'a'

        val value = createPrimitiveValueStub(expected)

        val actual = new ValueWrapper(value).value().asInstanceOf[Char]

        actual should be (expected)
      }

      it("should return a double if wrapping a DoubleValue") {
        val expected: Double = 3.0

        val value = createPrimitiveValueStub(expected)

        val actual = new ValueWrapper(value).value().asInstanceOf[Double]

        actual should be (expected)
      }

      it("should return a float if wrapping a FloatValue") {
        val expected: Float = 3.0f

        val value = createPrimitiveValueStub(expected)

        val actual = new ValueWrapper(value).value().asInstanceOf[Float]

        actual should be (expected)
      }

      it("should return an integer if wrapping an IntegerValue") {
        val expected: Int = 3

        val value = createPrimitiveValueStub(expected)

        val actual = new ValueWrapper(value).value().asInstanceOf[Int]

        actual should be (expected)
      }

      it("should return a long if wrapping a LongValue") {
        val expected: Long = 3

        val value = createPrimitiveValueStub(expected)

        val actual = new ValueWrapper(value).value().asInstanceOf[Long]

        actual should be (expected)
      }

      it("should return a short if wrapping a ShortValue") {
        val expected: Short = 3

        val value = createPrimitiveValueStub(expected)

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
        intercept[Throwable] {
          // TODO: Investigate creating a real value without needing a
          //       VirtualMachine instance
          // Represents an unknown primitive value that we are not checking
          new ValueWrapper(stub[PrimitiveValue]).value()
        }
      }
    }

    describe("#valueAsOption") {
      it("should return Some(value) if wrapped value has a value") {
        val expected = Some(true)

        val value = createPrimitiveValueStub(expected.get)

        val actual = new ValueWrapper(value).valueAsOption()
          .map(_.asInstanceOf[Boolean])

        actual should be (expected)
      }

      it("should return None if wrapped value does not have a value") {
        val expected = None
        val actual = new ValueWrapper(stub[ObjectReference]).valueAsOption()

        actual should be (expected)
      }
    }

    describe("#fieldsAndValues") {
      it("should return all fields available on the object") {
        val fieldsAndValues = Seq(
          (stub[Field], stub[PrimitiveValue]),
          (stub[Field], stub[ObjectReference])
        )
        val expected = fieldsAndValues.toMap

        val value = createObjectReferenceStub(
          fieldsAndValues = Some(fieldsAndValues))

        val actual = new ValueWrapper(value).fieldsAndValues()

        actual should be (expected)
      }

      it("should exclude the MODULE$ field (it is recursive)") {
        // Create the field that should be excluded
        val moduleField = createFieldStub("MODULE$")

        val fieldsAndValues = Seq(
          (stub[Field], stub[PrimitiveValue]),
          (stub[Field], stub[ObjectReference]),
          (moduleField, stub[Value])
        )

        // Expect all fields EXCEPT the module
        val expected = fieldsAndValues.filterNot(_._1 eq moduleField).toMap

        val value = createObjectReferenceStub(
          fieldsAndValues = Some(fieldsAndValues))

        val actual = new ValueWrapper(value).fieldsAndValues()

        actual should be (expected)
      }

      it("should fill in any missing values with null") {
        val fieldsAndValues = Seq(
          (stub[Field], stub[PrimitiveValue]),
          (stub[Field], stub[ObjectReference])
        )
        val fieldsWithNoValues = Seq(stub[Field])
        val expected =
          (fieldsAndValues ++ fieldsWithNoValues.map((_, null))).toMap

        val value = createObjectReferenceStub(
          fieldsAndValues = Some(fieldsAndValues),
          fieldsWithNoValues = Some(fieldsWithNoValues)
        )

        val actual = new ValueWrapper(value).fieldsAndValues()

        actual should be (expected)
      }

      it("should throw an exception if not an object reference") {
        intercept[Throwable] {
          new ValueWrapper(stub[PrimitiveValue]).value()
        }
      }
    }

    describe("#fieldsAndValuesAsOption") {
      it("should return Some(...) if wrapped value is capable of fields") {
        val fieldsAndValues = Seq(
          (stub[Field], stub[PrimitiveValue]),
          (stub[Field], stub[ObjectReference])
        )
        val expected = Some(fieldsAndValues.toMap)

        val value = createObjectReferenceStub(
          fieldsAndValues = Some(fieldsAndValues))

        val actual = new ValueWrapper(value).fieldsAndValuesAsOption()

        actual should be (expected)
      }

      it("should return None if wrapped value does not have fields") {
        val expected = None
        val actual =
          new ValueWrapper(stub[PrimitiveValue]).fieldsAndValuesAsOption()

        actual should be (expected)
      }
    }

    describe("#toString") {
      it("should print out the value as a string") {
        val value = stub[Value]

        val expected = value.toString
        val actual = new ValueWrapper(value).toString

        actual should be (expected)
      }

      it("should print out the field names and values if given a depth of 2") {

        val fieldsAndValues = Seq(
          (createFieldStub("one"), stub[PrimitiveValue]),
          (createFieldStub("two"), stub[ObjectReference])
        )
        val value = createObjectReferenceStub(
          fieldsAndValues = Some(fieldsAndValues))

        val expected = value.toString + "\n" +
          fieldsAndValues.map {
            case (f,v) => "\t" + f.name() + ": " + v.toString
          }.mkString("\n")
        val actual = new ValueWrapper(value).toString(2)

        actual should be (expected)
      }
    }
  }
}
