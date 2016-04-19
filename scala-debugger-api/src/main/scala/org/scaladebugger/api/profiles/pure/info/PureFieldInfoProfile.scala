package org.scaladebugger.api.profiles.pure.info
//import acyclic.file

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.{ValueInfoProfile, VariableInfoProfile}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.util.Try

/**
 * Represents a pure implementation of a field profile that adds no custom
 * logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            field
 * @param _objectReference The object associated with the field instance
 * @param _field The reference to the underlying JDI field
 * @param _virtualMachine The virtual machine used to mirror local values on
 *                       the remote JVM
 */
class PureFieldInfoProfile(
  val scalaVirtualMachine: ScalaVirtualMachine,
  private val _objectReference: ObjectReference,
  private val _field: Field
)(
  private val _virtualMachine: VirtualMachine = _field.virtualMachine()
) extends VariableInfoProfile {

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: Field = _field

  /**
   * Returns the name of the variable.
   *
   * @return The name of the variable
   */
  override def name: String = _field.name()

  /**
   * Returns whether or not this variable represents a field.
   *
   * @return True if a field, otherwise false
   */
  override def isField: Boolean = true

  /**
   * Returns whether or not this variable represents an argument.
   *
   * @return True if an argument, otherwise false
   */
  override def isArgument: Boolean = false

  /**
   * Returns whether or not this variable represents a local variable.
   *
   * @return True if a local variable, otherwise false
   */
  override def isLocal: Boolean = false

  /**
   * Sets the primitive value of this variable.
   *
   * @param value The new value for the variable
   * @return The new value
   */
  override def setValue(value: AnyVal): AnyVal = {
    import org.scaladebugger.api.lowlevel.wrappers.Implicits._
    val mirrorValue = _virtualMachine.mirrorOf(value)
    setFieldValue(mirrorValue)
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
    setFieldValue(mirrorValue)
    value
  }

  private def setFieldValue(value: Value): Unit = {
    assert(_objectReference != null, "Cannot set field value on reference type!")

    _objectReference.setValue(_field, value)
  }

  /**
   * Returns a profile representing the value of this variable.
   *
   * @return The profile representing the value
   */
  override def toValue: ValueInfoProfile = {
    assert(_objectReference != null,
      "Cannot get field value from reference type!")

    newValueProfile(
      _objectReference.getValue(_field)
    )
  }

  protected def newValueProfile(value: Value): ValueInfoProfile =
    new PureValueInfoProfile(scalaVirtualMachine, value)
}
