package org.senkbeil.debugger.api.virtualmachines

import com.sun.jdi.VirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.ManagerContainer
import org.senkbeil.debugger.api.profiles.ProfileManager

class ScalaVirtualMachineSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{
  private class TestManagerContainer extends ManagerContainer(
    null, null, null, null, null, null, null, null, null, null, null, null,
    null, null, null, null, null, null, null
  )

  private def newScalaVirtualMachine(managerContainer: ManagerContainer) =
    new Object with ScalaVirtualMachine {
      override val lowlevel: ManagerContainer = managerContainer

      override def initialize(startProcessingEvents: Boolean): Unit = {}
      override val underlyingVirtualMachine: VirtualMachine = null
      override def isStarted: Boolean = false
      override val uniqueId: String = ""
      override protected val profileManager: ProfileManager = null
    }

  private val mockManagerContainerProcessPendingRequests =
    mockFunction[ManagerContainer, Unit]
  private val scalaVirtualMachine = newScalaVirtualMachine(new TestManagerContainer {
    override def processPendingRequests(
      managerContainer: ManagerContainer
    ): Unit = mockManagerContainerProcessPendingRequests(managerContainer)
  })

  describe("ScalaVirtualMachine") {
    describe("#processPendingRequests") {
      it("should process the other VM's pending requests using its low-level managers") {
        val otherManagerContainer = new TestManagerContainer
        val otherScalaVirtualMachine = newScalaVirtualMachine(otherManagerContainer)

        mockManagerContainerProcessPendingRequests.expects(otherManagerContainer).once()

        scalaVirtualMachine.processPendingRequests(otherScalaVirtualMachine)
      }
    }
  }
}
