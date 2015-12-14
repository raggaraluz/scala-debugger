package org.senkbeil.debugger.api.lowlevel

import com.sun.jdi.request.EventRequestManager
import org.scalamock.scalatest.MockFactory
import org.scalatest.{OneInstancePerTest, Matchers, FunSpec}
import org.senkbeil.debugger.api.lowlevel.breakpoints.{PendingBreakpointSupport, DummyBreakpointManager, BreakpointManager}
import org.senkbeil.debugger.api.lowlevel.classes._
import org.senkbeil.debugger.api.lowlevel.events.{PendingEventHandlerSupport, DummyEventManager, EventManager}
import org.senkbeil.debugger.api.lowlevel.exceptions.{PendingExceptionSupport, DummyExceptionManager, ExceptionManager}
import org.senkbeil.debugger.api.lowlevel.methods._
import org.senkbeil.debugger.api.lowlevel.monitors._
import org.senkbeil.debugger.api.lowlevel.steps.{PendingStepSupport, DummyStepManager, StepManager}
import org.senkbeil.debugger.api.lowlevel.threads._
import org.senkbeil.debugger.api.lowlevel.vm.{PendingVMDeathSupport, DummyVMDeathManager, VMDeathManager}
import org.senkbeil.debugger.api.lowlevel.watchpoints._

class ManagerContainerSpec extends FunSpec with Matchers
  with MockFactory with OneInstancePerTest
{
  describe("ManagerContainer") {
    describe("#usingDummyManagers") {
      it("should create dummy managers supporting pending requests") {
        val managerContainer = ManagerContainer.usingDummyManagers()

        managerContainer.accessWatchpointManager shouldBe a [DummyAccessWatchpointManager]
        managerContainer.accessWatchpointManager shouldBe a [PendingAccessWatchpointSupport]
        managerContainer.breakpointManager shouldBe a [DummyBreakpointManager]
        managerContainer.breakpointManager shouldBe a [PendingBreakpointSupport]
        managerContainer.classManager should be (null)
        managerContainer.classPrepareManager shouldBe a [DummyClassPrepareManager]
        managerContainer.classPrepareManager shouldBe a [PendingClassPrepareSupport]
        managerContainer.classUnloadManager shouldBe a [DummyClassUnloadManager]
        managerContainer.classUnloadManager shouldBe a [PendingClassUnloadSupport]
        managerContainer.eventManager shouldBe a [DummyEventManager]
        managerContainer.eventManager shouldBe a [PendingEventHandlerSupport]
        managerContainer.exceptionManager shouldBe a [DummyExceptionManager]
        managerContainer.exceptionManager shouldBe a [PendingExceptionSupport]
        managerContainer.methodEntryManager shouldBe a [DummyMethodEntryManager]
        managerContainer.methodEntryManager shouldBe a [PendingMethodEntrySupport]
        managerContainer.methodExitManager shouldBe a [DummyMethodExitManager]
        managerContainer.methodExitManager shouldBe a [PendingMethodExitSupport]
        managerContainer.modificationWatchpointManager shouldBe a [DummyModificationWatchpointManager]
        managerContainer.modificationWatchpointManager shouldBe a [PendingModificationWatchpointSupport]
        managerContainer.monitorContendedEnteredManager shouldBe a [DummyMonitorContendedEnteredManager]
        managerContainer.monitorContendedEnteredManager shouldBe a [PendingMonitorContendedEnteredSupport]
        managerContainer.monitorContendedEnterManager shouldBe a [DummyMonitorContendedEnterManager]
        managerContainer.monitorContendedEnterManager shouldBe a [PendingMonitorContendedEnterSupport]
        managerContainer.monitorWaitedManager shouldBe a [DummyMonitorWaitedManager]
        managerContainer.monitorWaitedManager shouldBe a [PendingMonitorWaitedSupport]
        managerContainer.monitorWaitManager shouldBe a [DummyMonitorWaitManager]
        managerContainer.monitorWaitManager shouldBe a [PendingMonitorWaitSupport]
        managerContainer.requestManager should be (null)
        managerContainer.stepManager shouldBe a [DummyStepManager]
        managerContainer.stepManager shouldBe a [PendingStepSupport]
        managerContainer.threadDeathManager shouldBe a [DummyThreadDeathManager]
        managerContainer.threadDeathManager shouldBe a [PendingThreadDeathSupport]
        managerContainer.threadStartManager shouldBe a [DummyThreadStartManager]
        managerContainer.threadStartManager shouldBe a [PendingThreadStartSupport]
        managerContainer.vmDeathManager shouldBe a [DummyVMDeathManager]
        managerContainer.vmDeathManager shouldBe a [PendingVMDeathSupport]
      }
    }
    
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
