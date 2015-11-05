package org.senkbeil.debugger.api.profiles.pure.methods

import com.sun.jdi.VirtualMachine
import com.sun.jdi.event.MethodExitEvent
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.events.EventManager
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.lowlevel.events.filters.MethodNameFilter
import org.senkbeil.debugger.api.lowlevel.methods.MethodExitManager
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.utils.JDIRequestResponseBuilder
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.utils.LoopingTaskRunner
import test.JDIMockHelpers

import scala.util.{Success, Try}

class PureMethodExitProfileSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString

  // NOTE: Cannot mock the actual class and test the function using
  //       ScalaMock, so have to override the method we want to test and
  //       inject a mock function instead
  private val mockSetMethodExitFunc =
    mockFunction[String, String, Seq[JDIRequestArgument], Try[Boolean]]
  private val testMethodExitManager = new MethodExitManager(
    stub[VirtualMachine]
  ) {
    override def setMethodExit(
      className: String,
      methodName: String,
      extraArguments: JDIRequestArgument*
    ): Try[Boolean] = {
      mockSetMethodExitFunc(
        className,
        methodName,
        extraArguments
      )
    }
  }

  // Workaround - see https://github.com/paulbutcher/ScalaMock/issues/33
  private class ZeroArgEventManager extends EventManager(
    stub[VirtualMachine],
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

  private val pureMethodExitProfile = new Object with PureMethodExitProfile {
    override protected val methodExitManager = testMethodExitManager
    override protected val requestResponseBuilder = mockRequestResponseBuilder
  }

  describe("PureMethodExitProfile") {
    describe("#onMethodExitWithData") {
      it("should set a low-level method entry request and stream its events") {
        val expected = Success(Pipeline.newPipeline(
          classOf[(MethodExitEvent, Seq[JDIEventDataResult])]
        ))
        val className = "some.full.class.name"
        val methodName = "someMethodName"
        val arguments = Seq(mock[JDIRequestArgument])

        inSequence {
          expectCallAndInvokeRequestFunc[MethodExitEvent](
            mockRequestResponseBuilder,
            expected
          )

          mockSetMethodExitFunc.expects(
            className,
            methodName,
            arguments
          ).returning(Success(true)).once()
        }

        val actual = pureMethodExitProfile.onMethodExitWithData(
          className,
          methodName,
          arguments: _*
        )

        actual should be (expected)
      }

      it("should add a MethodNameFilter to the event stream") {
        val className = "some.full.class.name"
        val methodName = "someMethodName"
        val arguments = Seq(mock[JDIRequestArgument])

        inSequence {
          val returnValue = Success(Pipeline.newPipeline(
            classOf[(MethodExitEvent, Seq[JDIEventDataResult])]
          ))

          // Inspect the function arguments to see if MethodNameFilter included
          expectCallAndInvokeRequestFunc[MethodExitEvent](
            mockRequestResponseBuilder,
            returnValue
          ).map(_._2).foreach(_ should contain (MethodNameFilter(methodName)))

          mockSetMethodExitFunc.expects(
            className,
            methodName,
            arguments
          ).returning(Success(true)).once()
        }

        pureMethodExitProfile.onMethodExitWithData(
          className,
          methodName,
          arguments: _*
        )
      }
    }
  }
}
