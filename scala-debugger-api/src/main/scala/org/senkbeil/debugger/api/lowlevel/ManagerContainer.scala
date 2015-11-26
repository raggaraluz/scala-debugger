package org.senkbeil.debugger.api.lowlevel

import com.sun.jdi.VirtualMachine
import com.sun.jdi.request.EventRequestManager
import org.senkbeil.debugger.api.lowlevel.breakpoints.{ExtendedBreakpointManager, BreakpointManager}
import org.senkbeil.debugger.api.lowlevel.classes.{ClassPrepareManager, ClassUnloadManager, ClassManager}
import org.senkbeil.debugger.api.lowlevel.events.EventManager
import org.senkbeil.debugger.api.lowlevel.exceptions.ExceptionManager
import org.senkbeil.debugger.api.lowlevel.methods.{MethodEntryManager, MethodExitManager}
import org.senkbeil.debugger.api.lowlevel.monitors.{MonitorWaitManager, MonitorWaitedManager, MonitorContendedEnterManager, MonitorContendedEnteredManager}
import org.senkbeil.debugger.api.lowlevel.steps.StepManager
import org.senkbeil.debugger.api.lowlevel.threads.{ThreadStartManager, ThreadDeathManager}
import org.senkbeil.debugger.api.lowlevel.vm.VMDeathManager
import org.senkbeil.debugger.api.utils.LoopingTaskRunner

/**
 * Represents a container for low-level managers.
 */
case class ManagerContainer(
  breakpointManager: BreakpointManager,
  classManager: ClassManager,
  classPrepareManager: ClassPrepareManager,
  classUnloadManager: ClassUnloadManager,
  eventManager: EventManager,
  exceptionManager: ExceptionManager,
  methodEntryManager: MethodEntryManager,
  methodExitManager: MethodExitManager,
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
   *
   * @param virtualMachine The virtual machine whose managers to initialize
   *
   * @return The container holding all of the new managers
   */
  def fromVirtualMachine(virtualMachine: VirtualMachine): ManagerContainer = {
    val loopingTaskRunner = new LoopingTaskRunner()
    fromVirtualMachine(virtualMachine, loopingTaskRunner)
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
    loopingTaskRunner: LoopingTaskRunner
  ): ManagerContainer = {
    lazy val eventRequestManager = virtualMachine.eventRequestManager()
    lazy val eventQueue = virtualMachine.eventQueue()
    // TODO: Revert back to normal breakpoint manager and add pending breakpoint
    //       functionality somewhere more separate
    lazy val breakpointManager =
      //new BreakpointManager(eventRequestManager, classManager)
      new ExtendedBreakpointManager(eventRequestManager, classManager)
    lazy val classManager =
      new ClassManager(virtualMachine, loadClasses = true)
    lazy val classPrepareManager =
      new ClassPrepareManager(eventRequestManager)
    lazy val classUnloadManager =
      new ClassUnloadManager(eventRequestManager)
    lazy val eventManager =
      new EventManager(eventQueue, loopingTaskRunner)
    lazy val exceptionManager =
      new ExceptionManager(virtualMachine, eventRequestManager)
    lazy val methodEntryManager =
      new MethodEntryManager(eventRequestManager)
    lazy val methodExitManager =
      new MethodExitManager(eventRequestManager)
    lazy val monitorContendedEnteredManager =
      new MonitorContendedEnteredManager(eventRequestManager)
    lazy val monitorContendedEnterManager =
      new MonitorContendedEnterManager(eventRequestManager)
    lazy val monitorWaitedManager =
      new MonitorWaitedManager(eventRequestManager)
    lazy val monitorWaitManager =
      new MonitorWaitManager(eventRequestManager)
    lazy val requestManager =
      virtualMachine.eventRequestManager()
    lazy val stepManager =
      new StepManager(eventRequestManager)
    lazy val threadDeathManager =
      new ThreadDeathManager(eventRequestManager)
    lazy val threadStartManager =
      new ThreadStartManager(eventRequestManager)
    lazy val vmDeathManager =
      new VMDeathManager(eventRequestManager)

    ManagerContainer(
      breakpointManager               = breakpointManager,
      classManager                    = classManager,
      classPrepareManager             = classPrepareManager,
      classUnloadManager              = classUnloadManager,
      eventManager                    = eventManager,
      exceptionManager                = exceptionManager,
      methodEntryManager              = methodEntryManager,
      methodExitManager               = methodExitManager,
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
