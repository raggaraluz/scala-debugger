package com.senkbeil.debugger.wrapper

import com.senkbeil.utils.LogLike
import com.sun.jdi._

import scala.collection.JavaConverters._
import scala.util.{ Failure, Success, Try }

/**
 * Represents a wrapper around a stack frame, providing additional methods.
 *
 * @param _stackFrame The stack frame to wrap
 */
class StackFrameWrapper(private val _stackFrame: StackFrame) extends LogLike {
  require(_stackFrame != null, "Stack frame cannot be null!")

  /**
   * Attempts to retrieve the "this" object for the underlying stack frame.
   *
   * @return Some object reference if retrieved, otherwise None
   */
  def thisObjectAsOption: Option[ObjectReference] =
    Try(_stackFrame.thisObject()) match {
      case Success(obj) => Some(obj)
      case Failure(ex) => None
    }

  /**
   * Retrieves local variables and their values for the underlying stack frame.
   *
   * @return The map of local variables paired with their respective values
   */
  def localVisibleVariableMap(): Map[LocalVariable, Value] =
    Try(_stackFrame.visibleVariables()).map(_.asScala).getOrElse(Nil)
      .filterNot(_.isArgument)
      .map(variable => variable -> _stackFrame.getValue(variable))
      .toMap

  /**
   * Retrieves fields and values for the "this" object contained in the
   * underlying stack frame.
   *
   * @return The map of fields paired with their respective values
   */
  def thisVisibleFieldMap(): Map[Field, Value] = {
    val stackThisObject = thisObjectAsOption match {
      case Some(obj) => obj

      // Failed to get "this" object, so exit early
      case _ => return Map()
    }

    // Attempt to retrieve all visible fields and build a map of field -> value
    Try(stackThisObject.referenceType().visibleFields())
      .map(_.asScala).getOrElse(Nil)
      .map(field => field -> (Try(stackThisObject.getValue(field)) match {
        // Successfully retrieved field value, so just return it
        case Success(v) => v

        // Failed to retrieve value of field, so log and return null
        case Failure(ex) =>
          logger.throwable(ex)
          null
      })).toMap
  }
}
