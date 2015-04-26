package com.senkbeil.debugger.wrapper

import com.sun.jdi._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfter, Matchers, FunSpec}

import scala.collection.JavaConverters._

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

    describe("#valueAsOption") {
      it("should return Some(value) if wrapped value has a value") {
        val expected = Some(true)

        // TODO: Investigate creating a real value without needing a
        //       VirtualMachine instance
        // Set up the value to return
        val value = stub[BooleanValue]
        (value.value _).when().returns(expected.get)

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

        val value = createObjectReference(fieldsAndValues)

        val actual = new ValueWrapper(value).fieldsAndValues()

        actual should be (expected)
      }

      it("should exclude the MODULE$ field (it is recursive)") {
        // Create the field that should be excluded
        val moduleField = {
          val _field = stub[Field]
          (_field.name _).when().returns("MODULE$")
          _field
        }

        val fieldsAndValues = Seq(
          (stub[Field], stub[PrimitiveValue]),
          (stub[Field], stub[ObjectReference]),
          (moduleField, stub[Value])
        )

        // Expect all fields EXCEPT the module
        val expected = fieldsAndValues.filterNot(_._1 eq moduleField).toMap

        val value = createObjectReference(fieldsAndValues)

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

        val value = createObjectReference(fieldsAndValues, fieldsWithNoValues)

        val actual = new ValueWrapper(value).fieldsAndValues()

        actual should be (expected)
      }

      it("should throw an exception if not an object reference") {
        intercept[RuntimeException] {
          // TODO: Investigate creating a real value without needing a
          //       VirtualMachine instance
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

        val value = createObjectReference(fieldsAndValues)

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
        def createField(name: String) = {
          val _field = stub[Field]
          (_field.name _).when().returns(name)
          _field
        }

        val fieldsAndValues = Seq(
          (createField("one"), stub[PrimitiveValue]),
          (createField("two"), stub[ObjectReference])
        )
        val value = createObjectReference(fieldsAndValues)

        val expected = value.toString + "\n" +
          fieldsAndValues.map {
            case (f,v) => "\t" + f.name() + ": " + v.toString
          }.mkString("\n")
        val actual = new ValueWrapper(value).toString(2)

        actual should be (expected)
      }
    }
  }

  // ==========================================================================
  // = HELPER METHODS
  // ==========================================================================

  // TODO: Investigate creating a real value without needing a
  //       VirtualMachine instance
  def createObjectReference(
    fieldsAndValues: Seq[(Field, Value)],
    fieldsWithNoValues: Seq[Field] = Nil
  ) = {
    val value = stub[ObjectReference]

    // Set the visible fields to return the provided fields
    (value.referenceType _).when().returns({
      val referenceType = stub[ReferenceType]
      (referenceType.visibleFields _).when()
        .returns((fieldsAndValues.map(_._1) ++ fieldsWithNoValues).asJava)

      referenceType
    })

    // Set the retrieval of a field's value to the associated value
    fieldsAndValues.foreach { case (f, v) =>
      (value.getValue _).when(f).returns(v)
    }

    // Set the retrieval of extra fields as an exception
    fieldsWithNoValues.foreach { case f =>
      (value.getValue _).when(f).throws(new RuntimeException)
    }

    value
  }
}
