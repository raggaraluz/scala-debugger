package org.senkbeil.debugger.api.lowlevel

import com.sun.jdi.VirtualMachine
import com.sun.jdi.request.EventRequestManager
import org.senkbeil.debugger.api.lowlevel.breakpoints.BreakpointManager
import org.senkbeil.debugger.api.lowlevel.classes.ClassManager
import org.senkbeil.debugger.api.lowlevel.events.EventManager
import org.senkbeil.debugger.api.lowlevel.exceptions.ExceptionManager
import org.senkbeil.debugger.api.lowlevel.methods.{MethodExitManager, MethodEntryManager}
import org.senkbeil.debugger.api.lowlevel.steps.StepManager
import org.senkbeil.debugger.api.lowlevel.vm.VMDeathManager
import org.senkbeil.debugger.api.utils.LoopingTaskRunner

/**
 * Represents a container for low-level managers.
 */
case class ManagerContainer(
  breakpointManager: BreakpointManager,
  classManager: ClassManager,
  eventManager: EventManager,
  exceptionManager: ExceptionManager,
  methodEntryManager: MethodEntryManager,
  methodExitManager: MethodExitManager,
  requestManager: EventRequestManager,
  stepManager: StepManager,
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
    lazy val breakpointManager =
      new BreakpointManager(eventRequestManager, classManager)
    lazy val classManager =
      new ClassManager(virtualMachine, loadClasses = true)
    lazy val eventManager =
      new EventManager(eventQueue, loopingTaskRunner)
    lazy val exceptionManager =
      new ExceptionManager(virtualMachine, eventRequestManager)
    lazy val methodEntryManager =
      new MethodEntryManager(eventRequestManager)
    lazy val methodExitManager =
      new MethodExitManager(eventRequestManager)
    lazy val requestManager =
      virtualMachine.eventRequestManager()
    lazy val stepManager =
      new StepManager(eventRequestManager)
    lazy val vmDeathManager =
      new VMDeathManager(eventRequestManager)

    ManagerContainer(
      breakpointManager   = breakpointManager,
      classManager        = classManager,
      eventManager        = eventManager,
      exceptionManager    = exceptionManager,
      methodEntryManager  = methodEntryManager,
      methodExitManager   = methodExitManager,
      requestManager      = requestManager,
      stepManager         = stepManager,
      vmDeathManager      = vmDeathManager
    )
  }
}
