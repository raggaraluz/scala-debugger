package org.senkbeil.debugger.api.virtualmachines

import java.util.concurrent.atomic.AtomicBoolean

import org.senkbeil.debugger.api.lowlevel.ManagerContainer
import org.senkbeil.debugger.api.lowlevel.breakpoints.PendingBreakpointSupport
import org.senkbeil.debugger.api.lowlevel.utils.JDIHelperMethods
import org.senkbeil.debugger.api.profiles.pure.PureDebugProfile
import org.senkbeil.debugger.api.profiles.ProfileManager
import org.senkbeil.debugger.api.profiles.swappable.SwappableDebugProfile
import org.senkbeil.debugger.api.utils.{LoopingTaskRunner, Logging}
import com.sun.jdi._

import scala.util.Try

/**
 * Represents a virtual machine running Scala code.
 *
 * @param _virtualMachine The underlying virtual machine
 * @param profileManager The manager used to provide specific implementations
 *                       of debugging via profiles
 * @param loopingTaskRunner The runner used to process events from remote JVMs
 * @param uniqueId A unique id assigned to the Scala virtual machine on the
 *                 client (library) side to help distinguish multiple VMs
 */
class ScalaVirtualMachine(
  protected val _virtualMachine: VirtualMachine,
  protected val profileManager: ProfileManager,
  private val loopingTaskRunner: LoopingTaskRunner,
  val uniqueId: String = java.util.UUID.randomUUID().toString
) extends JDIHelperMethods with SwappableDebugProfile with Logging {
  private val started = new AtomicBoolean(false)

  /**
   * Indicates whether or not the virtual machine has started (received the
   * start event).
   *
   * @return True if started, otherwise false
   */
  def isStarted = started.get()

  /** Builds a string with the identifier of this virtual machine. */
  private def vmString(message: String) = s"(Scala VM $uniqueId) $message"

  /**
   * Creates a new instance of a manager container with newly-initialized
   * managers.
   *
   * @param loopingTaskRunner The looping task runner to provide to various
   *                          managers
   *
   * @return The new container of managers
   */
  protected def newManagerContainer(
    loopingTaskRunner: LoopingTaskRunner
  ): ManagerContainer = ManagerContainer.fromVirtualMachine(
    virtualMachine = _virtualMachine,
    loopingTaskRunner = loopingTaskRunner,
    autoStartEventManager = false
  )

  /** Represents the collection of low-level APIs for the virtual machine. */
  lazy val lowlevel = newManagerContainer(loopingTaskRunner)

  /**
   * Initializes the ScalaVirtualMachine system.
   *
   * @param startProcessingEvents If true, immediately starts processing events
   */
  def initialize(startProcessingEvents: Boolean = true): Unit = {
    logger.debug(vmString("Initializing Scala virtual machine!"))

    // Register our standard profiles
    profileManager.register(
      PureDebugProfile.Name,
      new PureDebugProfile(_virtualMachine, lowlevel)
    )

    // Mark our default profile
    this.use(PureDebugProfile.Name)

    logger.debug(vmString("Adding custom event handlers!"))

    // Mark start event to load all of our system classes
    this.withProfile(PureDebugProfile.Name).onUnsafeVMStart().foreach(_ => {
      // Mark the VM as started
      started.set(true)

      logger.trace(vmString("Refreshing all class references!"))
      lowlevel.classManager.refreshAllClasses()

      // TODO: Revert back to normal breakpoint manager and add pending
      //       breakpoint functionality somewhere more separate
      logger.trace(vmString("Applying any pending breakpoints for references!"))
      lowlevel.classManager.allFileNames.foreach(
        lowlevel.breakpointManager.asInstanceOf[PendingBreakpointSupport]
          .processPendingBreakpointRequestsForFile
      )
    })

    // Mark class prepare events to signal refreshing our classes
    this.withProfile(PureDebugProfile.Name)
      .onUnsafeClassPrepare().foreach(classPrepareEvent => {
      val referenceType = classPrepareEvent.referenceType()
      val referenceTypeName = referenceType.name()
      val fileName =
        lowlevel.classManager.fileNameForReferenceType(referenceType)

      logger.trace(vmString(s"Received new class: $referenceTypeName"))
      lowlevel.classManager.refreshClass(referenceType)

      // TODO: Revert back to normal breakpoint manager and add pending
      //       breakpoint functionality somewhere more separate
      logger.trace(vmString(
        s"Processing any pending breakpoints for $referenceTypeName!"))
      lowlevel.breakpointManager.asInstanceOf[PendingBreakpointSupport]
        .processPendingBreakpointRequestsForFile(fileName)
    })

    // Try to start the event manager if indicated
    if (startProcessingEvents) Try(lowlevel.eventManager.start())
  }

  /**
   * Represents the underlying virtual machine represented by this Scala
   * virtual machine.
   *
   * @return The JDI VirtualMachine instance
   */
  val underlyingVirtualMachine: VirtualMachine = _virtualMachine
}

