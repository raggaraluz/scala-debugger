package org.senkbeil.debugger.api.lowlevel

import com.sun.jdi.request.EventRequestManager
import org.scalamock.scalatest.MockFactory
import org.scalatest.{OneInstancePerTest, Matchers, FunSpec}
import org.senkbeil.debugger.api.lowlevel.breakpoints.BreakpointManager
import org.senkbeil.debugger.api.lowlevel.classes.{ClassUnloadManager, ClassPrepareManager, ClassManager}
import org.senkbeil.debugger.api.lowlevel.events.EventManager
import org.senkbeil.debugger.api.lowlevel.exceptions.ExceptionManager
import org.senkbeil.debugger.api.lowlevel.methods.{MethodExitManager, MethodEntryManager}
import org.senkbeil.debugger.api.lowlevel.monitors.{MonitorWaitManager, MonitorWaitedManager, MonitorContendedEnterManager, MonitorContendedEnteredManager}
import org.senkbeil.debugger.api.lowlevel.steps.StepManager
import org.senkbeil.debugger.api.lowlevel.threads.{ThreadStartManager, ThreadDeathManager}
import org.senkbeil.debugger.api.lowlevel.vm.VMDeathManager
import org.senkbeil.debugger.api.lowlevel.watchpoints.{ModificationWatchpointManager, AccessWatchpointManager}

class ManagerContainerSpec extends FunSpec with Matchers
  with MockFactory with OneInstancePerTest
{
  describe("ManagerContainer") {
    describe("#enablePendingSupport") {
      it("should enable pending support for any manager supporting pending requests") {
        val mockAccessWatchpointManager = mock[TestPendingAccessWatchpointManager]
        val mockBreakpointManager = mock[TestPendingBreakpointManager]
        val mockClassManager = mock[TestPendingClassManager]
        val mockClassPrepareManager = mock[TestPendingClassPrepareManager]
        val mockClassUnloadManager = mock[TestPendingClassUnloadManager]
        val mockEventManager = mock[TestPendingEventManager]
        val mockExceptionManager = mock[TestPendingExceptionManager]
        val mockMethodEntryManager = mock[TestPendingMethodEntryManager]
        val mockMethodExitManager = mock[TestPendingMethodExitManager]
        val mockModificationWatchpointManager = mock[TestPendingModificationWatchpointManager]
        val mockMonitorContendedEnteredManager = mock[TestPendingMonitorContendedEnteredManager]
        val mockMonitorContendedEnterManager = mock[TestPendingMonitorContendedEnterManager]
        val mockMonitorWaitedManager = mock[TestPendingMonitorWaitedManager]
        val mockMonitorWaitManager = mock[TestPendingMonitorWaitManager]
        val mockRequestManager = mock[TestPendingEventRequestManager]
        val mockStepManager = mock[TestPendingStepManager]
        val mockThreadDeathManager = mock[TestPendingThreadDeathManager]
        val mockThreadStartManager = mock[TestPendingThreadStartManager]
        val mockVMDeathManager = mock[TestPendingVMDeathManager]
        
        val managerContainer = ManagerContainer(
          mockAccessWatchpointManager,
          mockBreakpointManager,
          mockClassManager,
          mockClassPrepareManager,
          mockClassUnloadManager,
          mockEventManager,
          mockExceptionManager,
          mockMethodEntryManager,
          mockMethodExitManager,
          mockModificationWatchpointManager,
          mockMonitorContendedEnteredManager,
          mockMonitorContendedEnterManager,
          mockMonitorWaitedManager,
          mockMonitorWaitManager,
          mockRequestManager,
          mockStepManager,
          mockThreadDeathManager,
          mockThreadStartManager,
          mockVMDeathManager
        )

        (mockAccessWatchpointManager.setPendingSupport _).expects(true).once()
        (mockBreakpointManager.setPendingSupport _).expects(true).once()
        (mockClassManager.setPendingSupport _).expects(true).once()
        (mockClassPrepareManager.setPendingSupport _).expects(true).once()
        (mockClassUnloadManager.setPendingSupport _).expects(true).once()
        (mockEventManager.setPendingSupport _).expects(true).once()
        (mockExceptionManager.setPendingSupport _).expects(true).once()
        (mockMethodEntryManager.setPendingSupport _).expects(true).once()
        (mockMethodExitManager.setPendingSupport _).expects(true).once()
        (mockModificationWatchpointManager.setPendingSupport _).expects(true).once()
        (mockMonitorContendedEnteredManager.setPendingSupport _).expects(true).once()
        (mockMonitorContendedEnterManager.setPendingSupport _).expects(true).once()
        (mockMonitorWaitedManager.setPendingSupport _).expects(true).once()
        (mockMonitorWaitManager.setPendingSupport _).expects(true).once()
        (mockRequestManager.setPendingSupport _).expects(true).once()
        (mockStepManager.setPendingSupport _).expects(true).once()
        (mockThreadDeathManager.setPendingSupport _).expects(true).once()
        (mockThreadStartManager.setPendingSupport _).expects(true).once()
        (mockVMDeathManager.setPendingSupport _).expects(true).once()

        managerContainer.enablePendingSupport()
      }

      it("should do nothing for any manager not supporting pending requests") {
        val mockAccessWatchpointManager = mock[AccessWatchpointManager]
        val mockBreakpointManager = mock[BreakpointManager]
        val mockClassManager = mock[ClassManager]
        val mockClassPrepareManager = mock[ClassPrepareManager]
        val mockClassUnloadManager = mock[ClassUnloadManager]
        val mockEventManager = mock[EventManager]
        val mockExceptionManager = mock[ExceptionManager]
        val mockMethodEntryManager = mock[MethodEntryManager]
        val mockMethodExitManager = mock[MethodExitManager]
        val mockModificationWatchpointManager = mock[ModificationWatchpointManager]
        val mockMonitorContendedEnteredManager = mock[MonitorContendedEnteredManager]
        val mockMonitorContendedEnterManager = mock[MonitorContendedEnterManager]
        val mockMonitorWaitedManager = mock[MonitorWaitedManager]
        val mockMonitorWaitManager = mock[MonitorWaitManager]
        val mockRequestManager = mock[EventRequestManager]
        val mockStepManager = mock[StepManager]
        val mockThreadDeathManager = mock[ThreadDeathManager]
        val mockThreadStartManager = mock[ThreadStartManager]
        val mockVMDeathManager = mock[VMDeathManager]

        val managerContainer = ManagerContainer(
          mockAccessWatchpointManager,
          mockBreakpointManager,
          mockClassManager,
          mockClassPrepareManager,
          mockClassUnloadManager,
          mockEventManager,
          mockExceptionManager,
          mockMethodEntryManager,
          mockMethodExitManager,
          mockModificationWatchpointManager,
          mockMonitorContendedEnteredManager,
          mockMonitorContendedEnterManager,
          mockMonitorWaitedManager,
          mockMonitorWaitManager,
          mockRequestManager,
          mockStepManager,
          mockThreadDeathManager,
          mockThreadStartManager,
          mockVMDeathManager
        )

        managerContainer.enablePendingSupport()
      }
    }

    describe("#disablePendingSupport") {
      it("should disable pending support for any manager supporting pending requests") {
        val mockAccessWatchpointManager = mock[TestPendingAccessWatchpointManager]
        val mockBreakpointManager = mock[TestPendingBreakpointManager]
        val mockClassManager = mock[TestPendingClassManager]
        val mockClassPrepareManager = mock[TestPendingClassPrepareManager]
        val mockClassUnloadManager = mock[TestPendingClassUnloadManager]
        val mockEventManager = mock[TestPendingEventManager]
        val mockExceptionManager = mock[TestPendingExceptionManager]
        val mockMethodEntryManager = mock[TestPendingMethodEntryManager]
        val mockMethodExitManager = mock[TestPendingMethodExitManager]
        val mockModificationWatchpointManager = mock[TestPendingModificationWatchpointManager]
        val mockMonitorContendedEnteredManager = mock[TestPendingMonitorContendedEnteredManager]
        val mockMonitorContendedEnterManager = mock[TestPendingMonitorContendedEnterManager]
        val mockMonitorWaitedManager = mock[TestPendingMonitorWaitedManager]
        val mockMonitorWaitManager = mock[TestPendingMonitorWaitManager]
        val mockRequestManager = mock[TestPendingEventRequestManager]
        val mockStepManager = mock[TestPendingStepManager]
        val mockThreadDeathManager = mock[TestPendingThreadDeathManager]
        val mockThreadStartManager = mock[TestPendingThreadStartManager]
        val mockVMDeathManager = mock[TestPendingVMDeathManager]

        val managerContainer = ManagerContainer(
          mockAccessWatchpointManager,
          mockBreakpointManager,
          mockClassManager,
          mockClassPrepareManager,
          mockClassUnloadManager,
          mockEventManager,
          mockExceptionManager,
          mockMethodEntryManager,
          mockMethodExitManager,
          mockModificationWatchpointManager,
          mockMonitorContendedEnteredManager,
          mockMonitorContendedEnterManager,
          mockMonitorWaitedManager,
          mockMonitorWaitManager,
          mockRequestManager,
          mockStepManager,
          mockThreadDeathManager,
          mockThreadStartManager,
          mockVMDeathManager
        )

        (mockAccessWatchpointManager.setPendingSupport _).expects(false).once()
        (mockBreakpointManager.setPendingSupport _).expects(false).once()
        (mockClassManager.setPendingSupport _).expects(false).once()
        (mockClassPrepareManager.setPendingSupport _).expects(false).once()
        (mockClassUnloadManager.setPendingSupport _).expects(false).once()
        (mockEventManager.setPendingSupport _).expects(false).once()
        (mockExceptionManager.setPendingSupport _).expects(false).once()
        (mockMethodEntryManager.setPendingSupport _).expects(false).once()
        (mockMethodExitManager.setPendingSupport _).expects(false).once()
        (mockModificationWatchpointManager.setPendingSupport _).expects(false).once()
        (mockMonitorContendedEnteredManager.setPendingSupport _).expects(false).once()
        (mockMonitorContendedEnterManager.setPendingSupport _).expects(false).once()
        (mockMonitorWaitedManager.setPendingSupport _).expects(false).once()
        (mockMonitorWaitManager.setPendingSupport _).expects(false).once()
        (mockRequestManager.setPendingSupport _).expects(false).once()
        (mockStepManager.setPendingSupport _).expects(false).once()
        (mockThreadDeathManager.setPendingSupport _).expects(false).once()
        (mockThreadStartManager.setPendingSupport _).expects(false).once()
        (mockVMDeathManager.setPendingSupport _).expects(false).once()

        managerContainer.disablePendingSupport()
      }

      it("should do nothing for any manager not supporting pending requests") {
        val mockAccessWatchpointManager = mock[AccessWatchpointManager]
        val mockBreakpointManager = mock[BreakpointManager]
        val mockClassManager = mock[ClassManager]
        val mockClassPrepareManager = mock[ClassPrepareManager]
        val mockClassUnloadManager = mock[ClassUnloadManager]
        val mockEventManager = mock[EventManager]
        val mockExceptionManager = mock[ExceptionManager]
        val mockMethodEntryManager = mock[MethodEntryManager]
        val mockMethodExitManager = mock[MethodExitManager]
        val mockModificationWatchpointManager = mock[ModificationWatchpointManager]
        val mockMonitorContendedEnteredManager = mock[MonitorContendedEnteredManager]
        val mockMonitorContendedEnterManager = mock[MonitorContendedEnterManager]
        val mockMonitorWaitedManager = mock[MonitorWaitedManager]
        val mockMonitorWaitManager = mock[MonitorWaitManager]
        val mockRequestManager = mock[EventRequestManager]
        val mockStepManager = mock[StepManager]
        val mockThreadDeathManager = mock[ThreadDeathManager]
        val mockThreadStartManager = mock[ThreadStartManager]
        val mockVMDeathManager = mock[VMDeathManager]

        val managerContainer = ManagerContainer(
          mockAccessWatchpointManager,
          mockBreakpointManager,
          mockClassManager,
          mockClassPrepareManager,
          mockClassUnloadManager,
          mockEventManager,
          mockExceptionManager,
          mockMethodEntryManager,
          mockMethodExitManager,
          mockModificationWatchpointManager,
          mockMonitorContendedEnteredManager,
          mockMonitorContendedEnterManager,
          mockMonitorWaitedManager,
          mockMonitorWaitManager,
          mockRequestManager,
          mockStepManager,
          mockThreadDeathManager,
          mockThreadStartManager,
          mockVMDeathManager
        )

        managerContainer.disablePendingSupport()
      }
    }
  }

  //
  // NOTE: The following classes are explicitly created due to a limitation
  //       of ScalaMock where you cannot issue
  //
  //           mock[VMDeathManager with PendingRequestSupport]
  //
  trait TestPendingAccessWatchpointManager extends AccessWatchpointManager with PendingRequestSupport
  trait TestPendingBreakpointManager extends BreakpointManager with PendingRequestSupport
  trait TestPendingClassManager extends ClassManager with PendingRequestSupport
  trait TestPendingClassPrepareManager extends ClassPrepareManager with PendingRequestSupport
  trait TestPendingClassUnloadManager extends ClassUnloadManager with PendingRequestSupport
  trait TestPendingEventManager extends EventManager with PendingRequestSupport
  trait TestPendingExceptionManager extends ExceptionManager with PendingRequestSupport
  trait TestPendingMethodEntryManager extends MethodEntryManager with PendingRequestSupport
  trait TestPendingMethodExitManager extends MethodExitManager with PendingRequestSupport
  trait TestPendingModificationWatchpointManager extends ModificationWatchpointManager with PendingRequestSupport
  trait TestPendingMonitorContendedEnteredManager extends MonitorContendedEnteredManager with PendingRequestSupport
  trait TestPendingMonitorContendedEnterManager extends MonitorContendedEnterManager with PendingRequestSupport
  trait TestPendingMonitorWaitedManager extends MonitorWaitedManager with PendingRequestSupport
  trait TestPendingMonitorWaitManager extends MonitorWaitManager with PendingRequestSupport
  trait TestPendingEventRequestManager extends EventRequestManager with PendingRequestSupport
  trait TestPendingStepManager extends StepManager with PendingRequestSupport
  trait TestPendingThreadDeathManager extends ThreadDeathManager with PendingRequestSupport
  trait TestPendingThreadStartManager extends ThreadStartManager with PendingRequestSupport
  trait TestPendingVMDeathManager extends VMDeathManager with PendingRequestSupport
}
