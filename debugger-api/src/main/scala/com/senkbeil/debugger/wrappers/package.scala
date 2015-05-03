package com.senkbeil.debugger

import scala.language.implicitConversions

import com.sun.jdi.{ReferenceType, ThreadReference, StackFrame, Value}

/**
 * Contains helper implicits to convert to wrapper classes.
 */
package object wrappers {
  implicit def valueToWrapper(value: Value): ValueWrapper =
    new ValueWrapper(value)

  implicit def stackFrameToWrapper(stackFrame: StackFrame): StackFrameWrapper =
    new StackFrameWrapper(stackFrame)

  implicit def threadReferenceToWrapper(
    threadReference: ThreadReference
  ): ThreadReferenceWrapper = new ThreadReferenceWrapper(threadReference)

  implicit def referenceTypeToWrapper(
    referenceType: ReferenceType
  ): ReferenceTypeWrapper = new ReferenceTypeWrapper(referenceType)
}
