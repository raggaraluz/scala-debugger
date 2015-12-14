package org.senkbeil.debugger.api.virtualmachines

import com.sun.jdi._
import org.senkbeil.debugger.api.lowlevel.ManagerContainer
import org.senkbeil.debugger.api.profiles.swappable.SwappableDebugProfile

/**
 * Represents a virtual machine running Scala code.
 */
trait ScalaVirtualMachine extends SwappableDebugProfile {
  /**
   * Initializes the ScalaVirtualMachine system.
   *
   * @param startProcessingEvents If true, immediately starts processing events
   */
  def initialize(startProcessingEvents: Boolean = true): Unit

  /**
   * Indicates whether or not the virtual machine has started (received the
   * start event).
   *
   * @return True if started, otherwise false
   */
  def isStarted: Boolean

  /**
   * Represents the collection of low-level APIs for the virtual machine.
   *
   * @return The container of low-level managers
   */
  def lowlevel: ManagerContainer

  /**
   * A unique id assigned to the Scala virtual machine on the client (library)
   * side to help distinguish multiple VMs.
   *
   * @return The unique id as a string
   */
  def uniqueId: String

  /**
   * Represents the underlying virtual machine represented by this Scala
   * virtual machine.
   *
   * @return The JDI VirtualMachine instance
   */
  def underlyingVirtualMachine: VirtualMachine
}

