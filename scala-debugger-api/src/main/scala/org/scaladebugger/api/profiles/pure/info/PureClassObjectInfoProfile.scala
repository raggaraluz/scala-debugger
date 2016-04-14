package org.scaladebugger.api.profiles.pure.info

//import acyclic.file

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a pure implementation of a class object profile that adds no
 * custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            class object
 * @param classObjectReference The reference to the underlying JDI class object
 * @param virtualMachine The virtual machine associated with the class object
 * @param threadReference The thread associated with the class object
 *                        (for method invocation)
 * @param referenceType The reference type for this class object
 */
class PureClassObjectInfoProfile(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  private val classObjectReference: ClassObjectReference
)(
  private val virtualMachine: VirtualMachine = classObjectReference.virtualMachine(),
  private val threadReference: ThreadReference = classObjectReference.owningThread(),
  private val referenceType: ReferenceType = classObjectReference.referenceType()
) extends PureObjectInfoProfile(scalaVirtualMachine, classObjectReference)(
  virtualMachine = virtualMachine,
  threadReference = threadReference,
  referenceType = referenceType
) with ClassObjectInfoProfile {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: ClassObjectReference = classObjectReference

  /**
   * Retrieves the reference type information corresponding to this class
   * object.
   *
   * @return The reference type information
   */
  override def getReflectedType: ReferenceTypeInfoProfile = {
    newReferenceTypeProfile(classObjectReference.reflectedType())
  }
}
