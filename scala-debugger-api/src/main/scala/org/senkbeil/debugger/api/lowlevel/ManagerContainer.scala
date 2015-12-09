package org.senkbeil.debugger.api.lowlevel

import com.sun.jdi.VirtualMachine
import com.sun.jdi.request.EventRequestManager
import org.senkbeil.debugger.api.lowlevel.breakpoints.{BreakpointManager, ExtendedBreakpointManager, StandardBreakpointManager}
import org.senkbeil.debugger.api.lowlevel.classes._
import org.senkbeil.debugger.api.lowlevel.events.{StandardEventManager, EventManager}
import org.senkbeil.debugger.api.lowlevel.exceptions.{StandardExceptionManager, ExceptionManager}
import org.senkbeil.debugger.api.lowlevel.methods.{MethodExitManager, MethodEntryManager, StandardMethodEntryManager, StandardMethodExitManager}
import org.senkbeil.debugger.api.lowlevel.monitors._
import org.senkbeil.debugger.api.lowlevel.steps.{StepManager, StandardStepManager}
import org.senkbeil.debugger.api.lowlevel.threads.{ThreadStartManager, ThreadDeathManager, StandardThreadStartManager, StandardThreadDeathManager}
import org.senkbeil.debugger.api.lowlevel.vm.{VMDeathManager, StandardVMDeathManager}
import org.senkbeil.debugger.api.lowlevel.watchpoints.{AccessWatchpointManager, ModificationWatchpointManager, StandardModificationWatchpointManager, StandardAccessWatchpointManager}
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
)

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
    // TODO: Revert back to normal breakpoint manager and add pending breakpoint
    //       functionality somewhere more separate
    lazy val breakpointManager = new ExtendedBreakpointManager(
      new StandardBreakpointManager(eventRequestManager, classManager)
    )
    lazy val classManager =
      new StandardClassManager(virtualMachine, loadClasses = true)
    lazy val classPrepareManager =
      new StandardClassPrepareManager(eventRequestManager)
    lazy val classUnloadManager =
      new StandardClassUnloadManager(eventRequestManager)
    lazy val eventManager = new StandardEventManager(
      eventQueue,
      loopingTaskRunner,
      autoStart = autoStartEventManager
    )
    lazy val exceptionManager =
      new StandardExceptionManager(virtualMachine, eventRequestManager)
    lazy val methodEntryManager =
      new StandardMethodEntryManager(eventRequestManager)
    lazy val methodExitManager =
      new StandardMethodExitManager(eventRequestManager)
    lazy val modificationWatchpointManager =
      new StandardModificationWatchpointManager(eventRequestManager, classManager)
    lazy val monitorContendedEnteredManager =
      new StandardMonitorContendedEnteredManager(eventRequestManager)
    lazy val monitorContendedEnterManager =
      new StandardMonitorContendedEnterManager(eventRequestManager)
    lazy val monitorWaitedManager =
      new StandardMonitorWaitedManager(eventRequestManager)
    lazy val monitorWaitManager =
      new StandardMonitorWaitManager(eventRequestManager)
    lazy val requestManager =
      virtualMachine.eventRequestManager()
    lazy val stepManager =
      new StandardStepManager(eventRequestManager)
    lazy val threadDeathManager =
      new StandardThreadDeathManager(eventRequestManager)
    lazy val threadStartManager =
      new StandardThreadStartManager(eventRequestManager)
    lazy val vmDeathManager =
      new StandardVMDeathManager(eventRequestManager)

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
