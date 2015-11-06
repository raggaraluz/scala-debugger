package org.senkbeil.debugger.api.profiles.pure.threads

import com.sun.jdi.event.{EventQueue, ThreadStartEvent}
import com.sun.jdi.request.EventRequestManager
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.events.EventManager
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.threads.ThreadStartManager
import org.senkbeil.debugger.api.lowlevel.utils.JDIRequestResponseBuilder
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.utils.LoopingTaskRunner
import test.JDIMockHelpers

import scala.util.{Success, Try}

class PureThreadStartProfileSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString

  // NOTE: Cannot mock the actual class and test the function using
  //       ScalaMock, so have to override the method we want to test and
  //       inject a mock function instead
  private val mockSetThreadStartFunc = mockFunction[
    Seq[JDIRequestArgument],
    Try[ThreadStartManager#ThreadStartKey]
  ]
  private val testThreadStartManager = new ThreadStartManager(
    stub[EventRequestManager]
  ) {
    override def createThreadStartRequest(
      extraArguments: JDIRequestArgument*
    ): Try[ThreadStartManager#ThreadStartKey] = {
      mockSetThreadStartFunc(
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

  private val pureThreadStartProfile = new Object with PureThreadStartProfile {
    override protected val threadStartManager = testThreadStartManager
    override protected val requestResponseBuilder = mockRequestResponseBuilder
  }

  describe("PureThreadStartProfile") {
    describe("#onThreadStartWithData") {
      it("should set a low-level thread start request and stream its events") {
        val expected = Success(Pipeline.newPipeline(
          classOf[(ThreadStartEvent, Seq[JDIEventDataResult])]
        ))
        val arguments = Seq(mock[JDIRequestArgument])

        inSequence {
          expectCallAndInvokeRequestFunc[ThreadStartEvent](
            mockRequestResponseBuilder,
            expected
          )

          mockSetThreadStartFunc.expects(arguments)
            .returning(Success("")).once()
        }

        val actual = pureThreadStartProfile.onThreadStartWithData(arguments: _*)

        actual should be (expected)
      }

      it("should add a MethodNameFilter to the event stream") {
        val arguments = Seq(mock[JDIRequestArgument])

        inSequence {
          val returnValue = Success(Pipeline.newPipeline(
            classOf[(ThreadStartEvent, Seq[JDIEventDataResult])]
          ))

          // Inspect the function arguments to see if MethodNameFilter included
          expectCallAndInvokeRequestFunc[ThreadStartEvent](
            mockRequestResponseBuilder,
            returnValue
          )

          mockSetThreadStartFunc.expects(arguments)
            .returning(Success("")).once()
        }

        pureThreadStartProfile.onThreadStartWithData(arguments: _*)
      }
    }
  }
}
