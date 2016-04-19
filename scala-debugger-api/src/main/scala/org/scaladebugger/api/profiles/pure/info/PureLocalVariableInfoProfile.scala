package org.scaladebugger.api.profiles.pure.info
//import acyclic.file

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.{FrameInfoProfile, IndexedVariableInfoProfile, ValueInfoProfile, VariableInfoProfile}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.util.Try

/**
 * Represents a pure implementation of a local variable profile that adds no
 * custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            local variable
 * @param frame The frame associated with the local variable instance
 * @param _localVariable The reference to the underlying JDI local variable
 * @param offsetIndex The offset of the variable relative to the frame's
 *                    contained local variables
 * @param _virtualMachine The virtual machine used to mirror local values on
 *                       the remote JVM
 */
class PureLocalVariableInfoProfile(
  val scalaVirtualMachine: ScalaVirtualMachine,
  val frame: FrameInfoProfile,
  private val _localVariable: LocalVariable,
  val offsetIndex: Int
)(
  private val _virtualMachine: VirtualMachine = _localVariable.virtualMachine()
) extends IndexedVariableInfoProfile {
  private lazy val stackFrame = frame.toJdiInstance

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: LocalVariable = _localVariable

  /**
   * Returns the name of the variable.
   *
   * @return The name of the variable
   */
  override def name: String = _localVariable.name()

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
  override def isArgument: Boolean = _localVariable.isArgument

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
    val mirrorValue = _virtualMachine.mirrorOf(value)
    stackFrame.setValue(_localVariable, mirrorValue)
    value
  }

  /**
   * Sets the string value of this variable.
   *
   * @param value The new value for the variable
   * @return The new value
   */
  override def setValue(value: String): String = {
    val mirrorValue = _virtualMachine.mirrorOf(value)
    stackFrame.setValue(_localVariable, mirrorValue)
    value
  }

  /**
   * Returns a profile representing the value of this variable.
   *
   * @return The profile representing the value
   */
  override def toValue: ValueInfoProfile = newValueProfile(
    stackFrame.getValue(_localVariable)
  )

  protected def newValueProfile(value: Value): ValueInfoProfile =
    new PureValueInfoProfile(scalaVirtualMachine, value)
}
