package org.scaladebugger.api.profiles.pure.info

//import acyclic.file

import com.sun.jdi._
import org.scaladebugger.api.lowlevel.{InvokeNonVirtualArgument, InvokeSingleThreadedArgument, JDIArgument}
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.collection.JavaConverters._

/**
 * Represents a pure implementation of a class loader profile that adds no
 * custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            class loader
 * @param classLoaderReference The reference to the underlying JDI class loader
 * @param virtualMachine The virtual machine associated with the class loader
 * @param threadReference The thread associated with the class loader
 *                        (for method invocation)
 * @param referenceType The reference type for this class loader
 */
class PureClassLoaderInfoProfile(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  private val classLoaderReference: ClassLoaderReference
)(
  private val virtualMachine: VirtualMachine = classLoaderReference.virtualMachine(),
  private val threadReference: ThreadReference = classLoaderReference.owningThread(),
  private val referenceType: ReferenceType = classLoaderReference.referenceType()
) extends PureObjectInfoProfile(scalaVirtualMachine, classLoaderReference)(
  virtualMachine = virtualMachine,
  threadReference = threadReference,
  referenceType = referenceType
) with ClassLoaderInfoProfile {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: ClassLoaderReference = classLoaderReference

  /**
   * Retrieves all loaded classes defined by this class loader.
   *
   * @return The collection of reference types for the loaded classes
   */
  override def getDefinedClasses: Seq[ReferenceTypeInfoProfile] = {
    import scala.collection.JavaConverters._
    classLoaderReference.definedClasses().asScala.map(newReferenceTypeProfile)
  }

  /**
   * Retrieves all classes for which this class loader served as the initiating
   * loader.
   *
   * @return The collection of reference types for the initiated classes
   */
  override def getVisibleClasses: Seq[ReferenceTypeInfoProfile] = {
    import scala.collection.JavaConverters._
    classLoaderReference.visibleClasses().asScala.map(newReferenceTypeProfile)
  }
}
