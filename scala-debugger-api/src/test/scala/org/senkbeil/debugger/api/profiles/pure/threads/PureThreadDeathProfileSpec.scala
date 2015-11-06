package org.senkbeil.debugger.api.profiles.pure.threads

import com.sun.jdi.event.{EventQueue, ThreadDeathEvent}
import com.sun.jdi.request.EventRequestManager
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.events.EventManager
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.threads.ThreadDeathManager
import org.senkbeil.debugger.api.lowlevel.utils.JDIRequestResponseBuilder
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.utils.LoopingTaskRunner
import test.JDIMockHelpers

import scala.util.{Success, Try}

class PureThreadDeathProfileSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString

  // NOTE: Cannot mock the actual class and test the function using
  //       ScalaMock, so have to override the method we want to test and
  //       inject a mock function instead
  private val mockSetThreadDeathFunc = mockFunction[
    Seq[JDIRequestArgument],
    Try[ThreadDeathManager#ThreadDeathKey]
  ]
  private val testThreadDeathManager = new ThreadDeathManager(
    stub[EventRequestManager]
  ) {
    override def createThreadDeathRequest(
      extraArguments: JDIRequestArgument*
    ): Try[ThreadDeathManager#ThreadDeathKey] = {
      mockSetThreadDeathFunc(
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

  private val pureThreadDeathProfile = new Object with PureThreadDeathProfile {
    override protected val threadDeathManager = testThreadDeathManager
    override protected val requestResponseBuilder = mockRequestResponseBuilder
  }

  describe("PureThreadDeathProfile") {
    describe("#onThreadDeathWithData") {
      it("should set a low-level thread death request and stream its events") {
        val expected = Success(Pipeline.newPipeline(
          classOf[(ThreadDeathEvent, Seq[JDIEventDataResult])]
        ))
        val arguments = Seq(mock[JDIRequestArgument])

        inSequence {
          expectCallAndInvokeRequestFunc[ThreadDeathEvent](
            mockRequestResponseBuilder,
            expected
          )

          mockSetThreadDeathFunc.expects(arguments)
            .returning(Success("")).once()
        }

        val actual = pureThreadDeathProfile.onThreadDeathWithData(arguments: _*)

        actual should be (expected)
      }

      it("should add a MethodNameFilter to the event stream") {
        val arguments = Seq(mock[JDIRequestArgument])

        inSequence {
          val returnValue = Success(Pipeline.newPipeline(
            classOf[(ThreadDeathEvent, Seq[JDIEventDataResult])]
          ))

          // Inspect the function arguments to see if MethodNameFilter included
          expectCallAndInvokeRequestFunc[ThreadDeathEvent](
            mockRequestResponseBuilder,
            returnValue
          )

          mockSetThreadDeathFunc.expects(arguments)
            .returning(Success("")).once()
        }

        pureThreadDeathProfile.onThreadDeathWithData(arguments: _*)
      }
    }
  }
}
