package org.scaladebugger.api.profiles.traits.info
//import acyclic.file

import com.sun.jdi.Mirror

import scala.util.Try

/**
 * Represents the interface for variable-based interaction.
 */
trait VariableInfoProfile extends CommonInfoProfile {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: Mirror

  /**
   * Returns the name of the variable.
   *
   * @return The name of the variable
   */
  def name: String

  /**
   * Returns whether or not this variable represents a field.
   *
   * @return True if a field, otherwise false
   */
  def isField: Boolean

  /**
   * Returns whether or not this variable represents a local variable.
   *
   * @return True if a local variable, otherwise false
   */
  def isLocal: Boolean

  /**
   * Returns whether or not this variable represents an argument.
   *
   * @return True if an argument, otherwise false
   */
  def isArgument: Boolean

  /**
   * Returns a profile representing the value of this variable.
   *
   * @return Success containing the profile representing the value, otherwise
   *         a failure
   */
  def tryToValue: Try[ValueInfoProfile] = Try(toValue)

  /**
   * Returns a profile representing the value of this variable.
   *
   * @return The profile representing the value
   */
  def toValue: ValueInfoProfile

  /**
   * Sets the primitive value of this variable.
   *
   * @param value The new value for the variable
   * @return Success containing the value, otherwise a failure
   */
  def trySetValue(value: AnyVal): Try[AnyVal] = Try(setValue(value))

  /**
   * Sets the primitive value of this variable.
   *
   * @param value The new value for the variable
   * @return The new value
   */
  def setValue(value: AnyVal): AnyVal

  /**
   * Sets the string value of this variable.
   *
   * @param value The new value for the variable
   * @return Success containing the value, otherwise a failure
   */
  def trySetValue(value: String): Try[String] = Try(setValue(value))

  /**
   * Sets the string value of this variable.
   *
   * @param value The new value for the variable
   * @return The new value
   */
  def setValue(value: String): String

  /**
   * Returns a string presenting a better human-readable description of
   * the JDI instance.
   *
   * @return The human-readable description
   */
  override def toPrettyString: String = {
    val name = this.name
    val value = this.tryToValue.map(_.toPrettyString).getOrElse("???")
    s"$name = $value"
  }
}
