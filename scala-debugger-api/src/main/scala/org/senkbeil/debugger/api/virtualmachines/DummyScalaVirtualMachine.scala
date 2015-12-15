package org.senkbeil.debugger.api.virtualmachines

import com.sun.jdi._
import org.senkbeil.debugger.api.lowlevel.ManagerContainer
import org.senkbeil.debugger.api.profiles.ProfileManager
import org.senkbeil.debugger.api.profiles.swappable.SwappableDebugProfile

/**
 * Represents a virtual machine running Scala code whose operations do nothing.
 *
 * @param profileManager The manager used to provide specific implementations
 *                       of debugging via profiles
 */
class DummyScalaVirtualMachine(
  protected val profileManager: ProfileManager
) extends ScalaVirtualMachine {
  /**
   * Initializes the ScalaVirtualMachine system.
   *
   * @param startProcessingEvents If true, immediately starts processing events
   */
  override def initialize(startProcessingEvents: Boolean = true): Unit = {}

  /**
   * Indicates whether or not the virtual machine has started (received the
   * start event).
   *
   * @return True if started, otherwise false
   */
  override def isStarted: Boolean = false

  /**
   * Represents the collection of low-level APIs for the virtual machine.
   *
   * @return The container of low-level managers
   */
  override lazy val lowlevel: ManagerContainer = ManagerContainer.usingDummyManagers()

  /**
   * A unique id assigned to the Scala virtual machine on the client (library)
   * side to help distinguish multiple VMs.
   *
   * @return The unique id as a string
   */
  override lazy val uniqueId: String = java.util.UUID.randomUUID().toString

  /**
   * Represents the underlying virtual machine represented by this Scala
   * virtual machine.
   *
   * @return The JDI VirtualMachine instance
   */
  override val underlyingVirtualMachine: VirtualMachine = null
}

