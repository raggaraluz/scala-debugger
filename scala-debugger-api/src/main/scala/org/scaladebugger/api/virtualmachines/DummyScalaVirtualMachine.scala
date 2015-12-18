package org.scaladebugger.api.virtualmachines

import com.sun.jdi._
import org.scaladebugger.api.lowlevel.ManagerContainer
import org.scaladebugger.api.profiles.ProfileManager
import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfile

/**
 * Represents a virtual machine running Scala code whose operations do nothing.
 *
 * @param profileManager The manager used to provide specific implementations
 *                       of debugging via profiles
 */
class DummyScalaVirtualMachine(
  protected val profileManager: ProfileManager,
  override val lowlevel: ManagerContainer = ManagerContainer.usingDummyManagers()
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
   * A unique id assigned to the Scala virtual machine on the client (library)
   * side to help distinguish multiple VMs.
   *
   * @return The unique id as a string
   */
  override val uniqueId: String = java.util.UUID.randomUUID().toString

  /**
   * Represents the underlying virtual machine represented by this Scala
   * virtual machine.
   *
   * @return The JDI VirtualMachine instance
   */
  override val underlyingVirtualMachine: VirtualMachine = null
}

object DummyScalaVirtualMachine {
  /**
   * Creates a new instance of the dummy Scala virtual machine using a new
   * instance of the default profile manager.
   *
   * @return The new dummy Scala virtual machine
   */
  def newInstance(): DummyScalaVirtualMachine = {
    val managerContainer = ManagerContainer.usingDummyManagers()

    val dummyScalaVirtualMachine = new DummyScalaVirtualMachine(
      ProfileManager.newDefaultInstance(managerContainer),
      managerContainer
    )

    dummyScalaVirtualMachine.use(PureDebugProfile.Name)

    dummyScalaVirtualMachine
  }
}
