package org.scaladebugger.api.profiles.pure.info
//import acyclic.file

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.{TypeInfoProfile, ValueInfoProfile, VariableInfoProfile}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.util.Try

/**
 * Represents a pure implementation of a field profile that adds no custom
 * logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            field
 * @param _container Either the object or reference type containing the
 *                   field instance
 * @param _field The reference to the underlying JDI field
 * @param _virtualMachine The virtual machine used to mirror local values on
 *                       the remote JVM
 */
class PureFieldInfoProfile(
  val scalaVirtualMachine: ScalaVirtualMachine,
  private val _container: Either[ObjectReference, ReferenceType],
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
   * Returns the name of the type representing the variable.
   *
   * @return The type name as a string
   */
  override def typeName: String = _field.typeName()

  /**
   * Returns the type information for the variable.
   *
   * @return The profile containing type information
   */
  override def typeInfo: TypeInfoProfile = newTypeProfile(_field.`type`())

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

  private def setFieldValue(value: Value): Unit = _container match {
    case Left(_objectReference) => _objectReference.setValue(_field, value)
    case Right(_classType: ClassType) => _classType.setValue(_field, value)
    case Right(other) =>
      throw new Exception(s"Cannot set value for non-class ${other.name()}!")
  }

  /**
   * Returns a profile representing the value of this variable.
   *
   * @return The profile representing the value
   */
  override def toValue: ValueInfoProfile = newValueProfile(_container match {
    case Left(_objectReference) => _objectReference.getValue(_field)
    case Right(_referenceType)  => _referenceType.getValue(_field)
  })

  protected def newValueProfile(value: Value): ValueInfoProfile =
    new PureValueInfoProfile(scalaVirtualMachine, value)

  protected def newTypeProfile(_type: Type): TypeInfoProfile =
    new PureTypeInfoProfile(scalaVirtualMachine, _type)
}
