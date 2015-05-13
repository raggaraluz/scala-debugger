package com.senkbeil.debugger.wrappers

import com.sun.jdi._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers}
import test.JDIMockHelpers

class StackFrameWrapperSpec extends FunSpec with Matchers with MockFactory
  with JDIMockHelpers
{
  describe("StackFrameWrapper") {
    describe("constructor") {
      it("should throw an exception if wrapping a null pointer") {
        intercept[IllegalArgumentException] {
          new StackFrameWrapper(null)
        }
      }
    }

    describe("#thisObjectAsOption") {
      it("should return Some(ObjectReference) if able to retrieve this") {
        val expected = Some(stub[ObjectReference])
        val stackFrame = createStackFrameStub(thisObject = expected)

        val actual = new StackFrameWrapper(stackFrame).thisObjectAsOption

        actual should be (expected)
      }

      it("should return None if not able to retrieve this") {
        val expected = None
        val stackFrame = createStackFrameStub(thisObject = expected)

        val actual = new StackFrameWrapper(stackFrame).thisObjectAsOption

        actual should be (expected)
      }
    }

    describe("#localVisibleVariable") {
      it("should return None if unable to retrieve the variable") {
        fail()
      }

      it("should return null for the value if unable to retrieve it") {
        fail()
      }

      it("should return Some tuple with the variable and value if successful") {
        fail()
      }
    }

    describe("#localVisibleVariableMap") {
      it("should return an empty map if unable to retrieve variables") {
        val expected = Map[LocalVariable, Value]()

        // Throws exceptions if all visible variables marked none in our stub
        val stackFrame = createStackFrameStub(
          visibleVariablesWithValues = None,
          visibleVariablesWithoutValues = None
        )

        val actual = new StackFrameWrapper(stackFrame).localVisibleVariableMap()

        actual should be (expected)
      }

      it("should not include arguments in its variables") {
        val localVariablesAndValues = Map[LocalVariable, Value](
          createLocalVariableStub("one") -> createPrimitiveValueStub(3),
          createLocalVariableStub("two", isArgument = true) ->
            createPrimitiveValueStub(false),
          createLocalVariableStub("three") -> createPrimitiveValueStub(0.5)
        )
        val expected = localVariablesAndValues.filterNot(_._1.isArgument)

        val stackFrame = createStackFrameStub(
          visibleVariablesWithValues = Some(localVariablesAndValues.toSeq),
          visibleVariablesWithoutValues = None
        )

        val actual = new StackFrameWrapper(stackFrame).localVisibleVariableMap()

        actual should be (expected)
      }

      it("should fill in any missing values with null") {
        val localVariablesAndValues = Map[LocalVariable, Value](
          createLocalVariableStub("one") -> createPrimitiveValueStub(3),
          createLocalVariableStub("two", isArgument = true) ->
            createPrimitiveValueStub(false),
          createLocalVariableStub("three") -> createPrimitiveValueStub(0.5)
        )
        val localVariables = Map[LocalVariable, Value](
          createLocalVariableStub("four") -> null
        )
        val expected =
          localVariablesAndValues.filterNot(_._1.isArgument) ++ localVariables

        val stackFrame = createStackFrameStub(
          visibleVariablesWithValues = Some(localVariablesAndValues.toSeq),
          visibleVariablesWithoutValues = Some(localVariables.keys.toSeq)
        )

        val actual = new StackFrameWrapper(stackFrame).localVisibleVariableMap()

        actual should be (expected)
      }

      it("should return a map of variable -> value from local variables") {
        val localVariablesAndValues = Map[LocalVariable, Value](
          createLocalVariableStub("one") -> createPrimitiveValueStub(3),
          createLocalVariableStub("two") -> createPrimitiveValueStub(0.5)
        )
        val expected = localVariablesAndValues

        val stackFrame = createStackFrameStub(
          visibleVariablesWithValues = Some(localVariablesAndValues.toSeq),
          visibleVariablesWithoutValues = None
        )

        val actual = new StackFrameWrapper(stackFrame).localVisibleVariableMap()

        actual should be (expected)
      }
    }

    describe("#thisVisibleField") {
      it("should return an empty map if 'this' is unavailable") {
        fail()
      }

      it("should return None if unable to retrieve the field") {
        fail()
      }

      it("should return null for the value if unable to retrieve it") {
        fail()
      }

      it("should return Some tuple with the field and value if successful") {
        fail()
      }
    }

    describe("#thisVisibleFieldMap") {
      it("should return an empty map if 'this' is unavailable") {
        val expected = Map[Field, Value]()

        // Throws exception (which is caught) when none
        val stackFrame = createStackFrameStub(thisObject = None)

        val actual = new StackFrameWrapper(stackFrame).thisVisibleFieldMap()

        actual should be (expected)
      }

      it("should return an empty map if unable to retrieve 'this' fields") {
        val expected = Map[Field, Value]()

        // Throws exception (which is caught) when none
        val stackFrame = createStackFrameStub(
          thisObject = Some(createObjectReferenceStub())
        )

        val actual = new StackFrameWrapper(stackFrame).thisVisibleFieldMap()

        actual should be (expected)
      }

      it("should fill in any missing values with null") {
        val expected = Map[Field, Value](
          createFieldStub("one") -> null
        )

        val stackFrame = createStackFrameStub(
          thisObject = Some(createObjectReferenceStub(
            fieldsWithNoValues = Some(expected.keys.toSeq)
          ))
        )

        val actual = new StackFrameWrapper(stackFrame).thisVisibleFieldMap()

        actual should be (expected)
      }

      it("should return a map of field -> value from visible fields") {
        val expected = Map[Field, Value](
          createFieldStub("one") -> createPrimitiveValueStub(3),
          createFieldStub("two") -> createPrimitiveValueStub(false)
        )

        val stackFrame = createStackFrameStub(
          thisObject = Some(createObjectReferenceStub(
            fieldsAndValues = Some(expected.toSeq)
          ))
        )

        val actual = new StackFrameWrapper(stackFrame).thisVisibleFieldMap()

        actual should be (expected)
      }
    }
  }
}
