package org.scaladebugger.api.profiles.scala210.info

import com.sun.jdi.ReferenceType
import org.scaladebugger.api.profiles.pure.info.PureReferenceTypeInfoProfile
import org.scaladebugger.api.profiles.traits.info.{FieldVariableInfoProfile, InfoProducerProfile}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a Scala 2.10 implementation of a reference type profile.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            reference type
 * @param infoProducer The producer of info-based profile instances
 * @param _referenceType The reference to the underlying JDI reference type
 */
class Scala210ReferenceTypeInfoProfile(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  override protected val infoProducer: InfoProducerProfile,
  private val _referenceType: ReferenceType
) extends PureReferenceTypeInfoProfile(
  scalaVirtualMachine = scalaVirtualMachine,
  infoProducer = infoProducer,
  _referenceType = _referenceType
) {
  /**
   * Retrieves the visible field with the matching name.
   *
   * @param name The name of the field to retrieve
   * @return Some field as a variable info profile, or None if doesn't exist
   */
  override def fieldOption(name: String): Option[FieldVariableInfoProfile] =
    allFields.find(_.name == name)
}
