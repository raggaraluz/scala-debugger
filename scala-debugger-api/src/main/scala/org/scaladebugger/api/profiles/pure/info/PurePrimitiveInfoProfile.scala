package org.scaladebugger.api.profiles.pure.info

//import acyclic.file

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.{ArrayInfoProfile, ObjectInfoProfile, PrimitiveInfoProfile, ValueInfoProfile}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine


/**
 * Represents a pure implementation of a value profile that adds no custom
 * logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            primitive
 * @param _primitiveValue The reference to the underlying JDI value
 */
class PurePrimitiveInfoProfile(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  private val _primitiveValue: PrimitiveValue
) extends PureValueInfoProfile(
  scalaVirtualMachine,
  _primitiveValue
) with PrimitiveInfoProfile {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: PrimitiveValue = _primitiveValue

  /**
   * Returns the value as a value local to this JVM.
   *
   * @return The value as a local instance
   */
  override def toLocalValue: AnyVal = {
    import org.scaladebugger.api.lowlevel.wrappers.Implicits._
    _primitiveValue.primitiveValue()
  }

  /**
   * Returns whether or not this primitive is a boolean.
   *
   * @return True if the primitive is a boolean, otherwise false
   */
  override def isBoolean: Boolean = _primitiveValue.isInstanceOf[BooleanValue]

  /**
   * Returns whether or not this primitive is a float.
   *
   * @return True if the primitive is a float, otherwise false
   */
  override def isFloat: Boolean = _primitiveValue.isInstanceOf[FloatValue]

  /**
   * Returns whether or not this primitive is a double.
   *
   * @return True if the primitive is a double, otherwise false
   */
  override def isDouble: Boolean = _primitiveValue.isInstanceOf[DoubleValue]

  /**
   * Returns whether or not this primitive is a integer.
   *
   * @return True if the primitive is a integer, otherwise false
   */
  override def isInteger: Boolean = _primitiveValue.isInstanceOf[IntegerValue]

  /**
   * Returns whether or not this primitive is a long.
   *
   * @return True if the primitive is a long, otherwise false
   */
  override def isLong: Boolean = _primitiveValue.isInstanceOf[LongValue]

  /**
   * Returns whether or not this primitive is a char.
   *
   * @return True if the primitive is a char, otherwise false
   */
  override def isChar: Boolean = _primitiveValue.isInstanceOf[CharValue]

  /**
   * Returns whether or not this primitive is a byte.
   *
   * @return True if the primitive is a byte, otherwise false
   */
  override def isByte: Boolean = _primitiveValue.isInstanceOf[ByteValue]

  /**
   * Returns whether or not this primitive is a short.
   *
   * @return True if the primitive is a short, otherwise false
   */
  override def isShort: Boolean = _primitiveValue.isInstanceOf[ShortValue]
}
