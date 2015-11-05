package org.senkbeil.debugger.api.profiles.pure.vm

import com.sun.jdi.VirtualMachine
import com.sun.jdi.event.{EventQueue, VMDeathEvent}
import com.sun.jdi.request.EventRequestManager
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.events.EventManager
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.utils.JDIRequestResponseBuilder
import org.senkbeil.debugger.api.lowlevel.vm.VMDeathManager
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.utils.LoopingTaskRunner
import test.JDIMockHelpers

import scala.util.{Success, Try}

class PureVMDeathProfileSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString

  // NOTE: Cannot mock the actual class and test the function using
  //       ScalaMock, so have to override the method we want to test and
  //       inject a mock function instead
  private val mockSetVMDeathFunc =
    mockFunction[Seq[JDIRequestArgument], Try[VMDeathManager#VMDeathKey]]
  private val testVMDeathManager = new VMDeathManager(
    stub[EventRequestManager]
  ) {
    override def createVMDeathRequest(
      extraArguments: JDIRequestArgument*
    ): Try[VMDeathManager#VMDeathKey] = {
      mockSetVMDeathFunc(
        extraArguments
      )
    }
  }

  // Workaround - see https://github.com/paulbutcher/ScalaMock/issues/33
  private class ZeroArgEventManager extends EventManager(
    stub[EventQueue],
    stub[LoopingTaskRunner],
    autoStart = false
  )
  private val mockEventManager = mock[ZeroArgEventManager]

  // Workaround - see https://github.com/paulbutcher/ScalaMock/issues/33
  private class ZeroArgRequestResponseBuilder extends JDIRequestResponseBuilder(
    mockEventManager
  ) {
    override protected def newRequestId(): String = TestRequestId
  }
  private val mockRequestResponseBuilder = mock[ZeroArgRequestResponseBuilder]

  private val pureVMDeathProfile = new Object with PureVMDeathProfile {
    override protected val vmDeathManager = testVMDeathManager
    override protected val requestResponseBuilder = mockRequestResponseBuilder
  }

  describe("PureVMDeathProfile") {
    describe("#onVMDeathWithData") {
      it("should set a low-level method entry request and stream its events") {
        val expected = Success(Pipeline.newPipeline(
          classOf[(VMDeathEvent, Seq[JDIEventDataResult])]
        ))
        val arguments = Seq(mock[JDIRequestArgument])

        inSequence {
          expectCallAndInvokeRequestFunc[VMDeathEvent](
            mockRequestResponseBuilder,
            expected
          )

          mockSetVMDeathFunc.expects(arguments).returning(Success("")).once()
        }

        val actual = pureVMDeathProfile.onVMDeathWithData(arguments: _*)

        actual should be (expected)
      }

      it("should add a MethodNameFilter to the event stream") {
        val arguments = Seq(mock[JDIRequestArgument])

        inSequence {
          val returnValue = Success(Pipeline.newPipeline(
            classOf[(VMDeathEvent, Seq[JDIEventDataResult])]
          ))

          // Inspect the function arguments to see if MethodNameFilter included
          expectCallAndInvokeRequestFunc[VMDeathEvent](
            mockRequestResponseBuilder,
            returnValue
          )

          mockSetVMDeathFunc.expects(arguments).returning(Success("")).once()
        }

        pureVMDeathProfile.onVMDeathWithData(arguments: _*)
      }
    }
  }
}
