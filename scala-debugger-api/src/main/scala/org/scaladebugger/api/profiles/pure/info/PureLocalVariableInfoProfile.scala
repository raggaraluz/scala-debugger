package org.scaladebugger.api.profiles.pure.info
//import acyclic.file

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.{FrameInfoProfile, IndexedVariableInfoProfile, ValueInfoProfile, VariableInfoProfile}

import scala.util.Try

/**
 * Represents a pure implementation of a local variable profile that adds no
 * custom logic on top of the standard JDI.
 *
 * @param frame The frame associated with the local variable instance
 * @param localVariable The reference to the underlying JDI local variable
 * @param offsetIndex The offset of the variable relative to the frame's
 *                    contained local variables
 * @param virtualMachine The virtual machine used to mirror local values on
 *                       the remote JVM
 */
class PureLocalVariableInfoProfile(
  val frame: FrameInfoProfile,
  private val localVariable: LocalVariable,
  val offsetIndex: Int
)(
  private val virtualMachine: VirtualMachine = localVariable.virtualMachine()
) extends IndexedVariableInfoProfile {
  private lazy val stackFrame = frame.toJdiInstance

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: LocalVariable = localVariable

  /**
   * Returns the name of the variable.
   *
   * @return The name of the variable
   */
  override def name: String = localVariable.name()

  /**
   * Returns the index of the stack frame where this variable is located.
   *
   * @return The frame starting from 0 (top of the stack)
   */
  override def frameIndex: Int = frame.index

  /**
   * Returns whether or not this variable represents a field.
   *
   * @return True if a field, otherwise false
   */
  override def isField: Boolean = false

  /**
   * Returns whether or not this variable represents an argument.
   *
   * @return True if an argument, otherwise false
   */
  override def isArgument: Boolean = localVariable.isArgument

  /**
   * Returns whether or not this variable represents a local variable.
   *
   * @return True if a local variable, otherwise false
   */
  override def isLocal: Boolean = true

  /**
   * Sets the primitive value of this variable.
   *
   * @param value The new value for the variable
   * @return The new value
   */
  override def setValue(value: AnyVal): AnyVal = {
    import org.scaladebugger.api.lowlevel.wrappers.Implicits._
    val mirrorValue = virtualMachine.mirrorOf(value)
    stackFrame.setValue(localVariable, mirrorValue)
    value
  }

  /**
   * Sets the string value of this variable.
   *
   * @param value The new value for the variable
   * @return The new value
   */
  override def setValue(value: String): String = {
    val mirrorValue = virtualMachine.mirrorOf(value)
    stackFrame.setValue(localVariable, mirrorValue)
    value
  }

  /**
   * Returns a profile representing the value of this variable.
   *
   * @return The profile representing the value
   */
  override def toValue: ValueInfoProfile = newValueProfile(
    stackFrame.getValue(localVariable)
  )

  protected def newValueProfile(value: Value): ValueInfoProfile =
    new PureValueInfoProfile(value)
}
