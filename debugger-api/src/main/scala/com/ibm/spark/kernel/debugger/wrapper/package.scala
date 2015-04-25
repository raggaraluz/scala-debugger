package com.ibm.spark.kernel.debugger

import scala.language.implicitConversions

import com.sun.jdi.{ ThreadReference, StackFrame, Value }

/**
 * Contains helper implicits to convert to wrapper classes.
 */
package object wrapper {
  implicit def valueToWrapper(value: Value): ValueWrapper =
    new ValueWrapper(value)

  implicit def stackFrameToWrapper(stackFrame: StackFrame): StackFrameWrapper =
    new StackFrameWrapper(stackFrame)

  implicit def threadReferenceToWrapper(
    threadReference: ThreadReference
  ): ThreadReferenceWrapper = new ThreadReferenceWrapper(threadReference)
}
