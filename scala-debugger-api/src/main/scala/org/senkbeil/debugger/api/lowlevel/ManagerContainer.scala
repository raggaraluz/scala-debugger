package org.senkbeil.debugger.api.lowlevel

import com.sun.jdi.VirtualMachine
import com.sun.jdi.request.EventRequestManager
import org.senkbeil.debugger.api.lowlevel.breakpoints.{StandardPendingBreakpointSupport, BreakpointManager, PendingBreakpointSupport, StandardBreakpointManager}
import org.senkbeil.debugger.api.lowlevel.classes._
import org.senkbeil.debugger.api.lowlevel.events.{StandardEventManager, EventManager}
import org.senkbeil.debugger.api.lowlevel.exceptions.{StandardPendingExceptionSupport, StandardExceptionManager, ExceptionManager}
import org.senkbeil.debugger.api.lowlevel.methods._
import org.senkbeil.debugger.api.lowlevel.monitors._
import org.senkbeil.debugger.api.lowlevel.steps.{StandardPendingStepSupport, StepManager, StandardStepManager}
import org.senkbeil.debugger.api.lowlevel.threads._
import org.senkbeil.debugger.api.lowlevel.vm.{StandardPendingVMDeathSupport, VMDeathManager, StandardVMDeathManager}
import org.senkbeil.debugger.api.lowlevel.watchpoints._
import org.senkbeil.debugger.api.utils.LoopingTaskRunner

/**
 * Represents a container for low-level managers.
 */
case class ManagerContainer(
  accessWatchpointManager: AccessWatchpointManager,
  breakpointManager: BreakpointManager,
  classManager: ClassManager,
  classPrepareManager: ClassPrepareManager,
  classUnloadManager: ClassUnloadManager,
  eventManager: EventManager,
  exceptionManager: ExceptionManager,
  methodEntryManager: MethodEntryManager,
  methodExitManager: MethodExitManager,
  modificationWatchpointManager: ModificationWatchpointManager,
  monitorContendedEnteredManager: MonitorContendedEnteredManager,
  monitorContendedEnterManager: MonitorContendedEnterManager,
  monitorWaitedManager: MonitorWaitedManager,
  monitorWaitManager: MonitorWaitManager,
  requestManager: EventRequestManager,
  stepManager: StepManager,
  threadDeathManager: ThreadDeathManager,
  threadStartManager: ThreadStartManager,
  vmDeathManager: VMDeathManager
) {
  /** Enables pending support for all managers supporting pending requests. */
  def enablePendingSupport(): Unit = setPendingSupportForAll(true)

  /** Disables pending support for all managers supporting pending requests. */
  def disablePendingSupport(): Unit = setPendingSupportForAll(false)

  /**
   * Sets the pending support enablement to the specified value.
   *
   * @param value True if enabling pending support on managers, otherwise false
   */
  private def setPendingSupportForAll(value: Boolean): Unit = {
    this.productIterator.foreach {
      case p: PendingRequestSupport => p.setPendingSupport(value)
      case _                        => // Do nothing in this case
    }
  }
}

/** Containers helper methods for initializing a manager container. */
object ManagerContainer {
  /**
   * Initializes all managers for the specified virtual machine. Uses the
   * default instance of the looping task runner for created managers.
   * Automatically starts the event manager.
   *
   * @param virtualMachine The virtual machine whose managers to initialize
   *
   * @return The container holding all of the new managers
   */
  def fromVirtualMachine(virtualMachine: VirtualMachine): ManagerContainer = {
    val loopingTaskRunner = new LoopingTaskRunner()
    fromVirtualMachine(
      virtualMachine,
      loopingTaskRunner,
      autoStartEventManager = true
    )
  }

  /**
   * Initializes all managers for the specified virtual machine.
   *
   * @param virtualMachine The virtual machine whose managers to initialize
   * @param loopingTaskRunner The task runner to use with various managers
   *
   * @return The container holding all of the new managers
   */
  def fromVirtualMachine(
    virtualMachine: VirtualMachine,
    loopingTaskRunner: LoopingTaskRunner,
    autoStartEventManager: Boolean
  ): ManagerContainer = {
    lazy val eventRequestManager = virtualMachine.eventRequestManager()
    lazy val eventQueue = virtualMachine.eventQueue()
    lazy val accessWatchpointManager =
      new StandardAccessWatchpointManager(eventRequestManager, classManager)
        with StandardPendingAccessWatchpointSupport
    lazy val breakpointManager =
      new StandardBreakpointManager(eventRequestManager, classManager)
        with StandardPendingBreakpointSupport
    lazy val classManager =
      new StandardClassManager(virtualMachine, loadClasses = true)
    lazy val classPrepareManager =
      new StandardClassPrepareManager(eventRequestManager)
        with StandardPendingClassPrepareSupport
    lazy val classUnloadManager =
      new StandardClassUnloadManager(eventRequestManager)
        with StandardPendingClassUnloadSupport
    lazy val eventManager = new StandardEventManager(
      eventQueue,
      loopingTaskRunner,
      autoStart = autoStartEventManager
    )
    lazy val exceptionManager =
      new StandardExceptionManager(virtualMachine, eventRequestManager)
        with StandardPendingExceptionSupport
    lazy val methodEntryManager =
      new StandardMethodEntryManager(eventRequestManager)
        with StandardPendingMethodEntrySupport
    lazy val methodExitManager =
      new StandardMethodExitManager(eventRequestManager)
    lazy val modificationWatchpointManager =
      new StandardModificationWatchpointManager(eventRequestManager, classManager)
        with StandardPendingModificationWatchpointSupport
    lazy val monitorContendedEnteredManager =
      new StandardMonitorContendedEnteredManager(eventRequestManager)
        with StandardPendingMonitorContendedEnteredSupport
    lazy val monitorContendedEnterManager =
      new StandardMonitorContendedEnterManager(eventRequestManager)
        with StandardPendingMonitorContendedEnterSupport
    lazy val monitorWaitedManager =
      new StandardMonitorWaitedManager(eventRequestManager)
        with StandardPendingMonitorWaitedSupport
    lazy val monitorWaitManager =
      new StandardMonitorWaitManager(eventRequestManager)
        with StandardPendingMonitorWaitSupport
    lazy val requestManager =
      virtualMachine.eventRequestManager()
    lazy val stepManager =
      new StandardStepManager(eventRequestManager)
        with StandardPendingStepSupport
    lazy val threadDeathManager =
      new StandardThreadDeathManager(eventRequestManager)
        with StandardPendingThreadDeathSupport
    lazy val threadStartManager =
      new StandardThreadStartManager(eventRequestManager)
        with StandardPendingThreadStartSupport
    lazy val vmDeathManager =
      new StandardVMDeathManager(eventRequestManager)
        with StandardPendingVMDeathSupport

    ManagerContainer(
      accessWatchpointManager         = accessWatchpointManager,
      breakpointManager               = breakpointManager,
      classManager                    = classManager,
      classPrepareManager             = classPrepareManager,
      classUnloadManager              = classUnloadManager,
      eventManager                    = eventManager,
      exceptionManager                = exceptionManager,
      methodEntryManager              = methodEntryManager,
      methodExitManager               = methodExitManager,
      modificationWatchpointManager   = modificationWatchpointManager,
      monitorContendedEnteredManager  = monitorContendedEnteredManager,
      monitorContendedEnterManager    = monitorContendedEnterManager,
      monitorWaitedManager            = monitorWaitedManager,
      monitorWaitManager              = monitorWaitManager,
      requestManager                  = requestManager,
      stepManager                     = stepManager,
      threadDeathManager              = threadDeathManager,
      threadStartManager              = threadStartManager,
      vmDeathManager                  = vmDeathManager
    )
  }
}
