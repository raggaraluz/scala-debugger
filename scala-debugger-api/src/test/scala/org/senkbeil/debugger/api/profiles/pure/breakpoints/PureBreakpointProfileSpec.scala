package org.senkbeil.debugger.api.profiles.pure.breakpoints

import com.sun.jdi.VirtualMachine
import com.sun.jdi.event.{EventQueue, BreakpointEvent}
import com.sun.jdi.request.EventRequestManager
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.breakpoints.BreakpointManager
import org.senkbeil.debugger.api.lowlevel.classes.ClassManager
import org.senkbeil.debugger.api.lowlevel.events.EventManager
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.utils.JDIRequestResponseBuilder
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.utils.LoopingTaskRunner
import test.JDIMockHelpers

import scala.util.{Try, Success}

class PureBreakpointProfileSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString

  // Workaround - see https://github.com/paulbutcher/ScalaMock/issues/33
  private class ZeroArgClassManager extends ClassManager(
    stub[VirtualMachine], loadClasses = false
  )
  private val stubClassManager = stub[ZeroArgClassManager]

  // Workaround - see https://github.com/paulbutcher/ScalaMock/issues/33
  private class ZeroArgBreakpointManager extends BreakpointManager(
    stub[EventRequestManager],
    stubClassManager
  )

  // NOTE: Cannot mock the actual class and test the function using
  //       ScalaMock, so have to override the method we want to test and
  //       inject a mock function instead
  private val mockSetLineBreakpointFunc =
    mockFunction[String, Int, Seq[JDIRequestArgument], Try[Boolean]]
  private val testBreakpointManager = new BreakpointManager(
    stub[EventRequestManager],
    stub[ZeroArgClassManager]
  ) {
    override def createLineBreakpointRequest(
      fileName: String,
      lineNumber: Int,
      extraArguments: JDIRequestArgument*
    ): Try[Boolean] = mockSetLineBreakpointFunc(
      fileName,
      lineNumber,
      extraArguments
    )
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

  private val pureBreakpointProfile = new Object with PureBreakpointProfile {
    override protected val breakpointManager = testBreakpointManager
    override protected val requestResponseBuilder = mockRequestResponseBuilder
  }

  describe("PureBreakpointProfile") {
    describe("#onBreakpointWithData") {
      it("should set a low-level breakpoint and stream its events") {
        val expected = Success(Pipeline.newPipeline(
          classOf[(BreakpointEvent, Seq[JDIEventDataResult])]
        ))
        val fileName = "some file"
        val lineNumber = 999
        val arguments = Seq(mock[JDIRequestArgument])

        inSequence {
          expectCallAndInvokeRequestFunc[BreakpointEvent](
            mockRequestResponseBuilder,
            expected
          )

          mockSetLineBreakpointFunc.expects(
            fileName,
            lineNumber,
            arguments
          ).returning(Success(true)).once()
        }

        val actual = pureBreakpointProfile.onBreakpointWithData(
          fileName,
          lineNumber,
          arguments: _*
        )

        actual should be (expected)
      }
    }
  }
}
