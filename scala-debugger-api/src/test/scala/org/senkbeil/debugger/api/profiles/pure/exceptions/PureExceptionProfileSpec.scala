package org.senkbeil.debugger.api.profiles.pure.exceptions

import com.sun.jdi.VirtualMachine
import com.sun.jdi.event.ExceptionEvent
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.exceptions.ExceptionManager
import org.senkbeil.debugger.api.lowlevel.classes.ClassManager
import org.senkbeil.debugger.api.lowlevel.events.EventManager
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.utils.JDIRequestResponseBuilder
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.utils.LoopingTaskRunner
import test.JDIMockHelpers

import scala.util.{Success, Try}

class PureExceptionProfileSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString

  // NOTE: Cannot mock the actual class and test the function using
  //       ScalaMock, so have to override the method we want to test and
  //       inject a mock function instead
  private val mockSetCatchallExceptionFunc =
    mockFunction[Boolean, Boolean, Seq[JDIRequestArgument], Try[Boolean]]
  private val mockSetExceptionFunc =
    mockFunction[String, Boolean, Boolean, Seq[JDIRequestArgument], Try[Boolean]]
  private val testExceptionManager = new ExceptionManager(
    stub[VirtualMachine]
  ) {
    override def setCatchallException(
      notifyCaught: Boolean,
      notifyUncaught: Boolean,
      extraArguments: JDIRequestArgument*
    ): Try[Boolean] = {
      mockSetCatchallExceptionFunc(
        notifyCaught,
        notifyUncaught,
        extraArguments
      )
    }

    override def setException(
      exceptionName: String,
      notifyCaught: Boolean,
      notifyUncaught: Boolean,
      extraArguments: JDIRequestArgument*
    ): Try[Boolean] = {
      mockSetExceptionFunc(
        exceptionName,
        notifyCaught,
        notifyUncaught,
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

  private val pureExceptionProfile = new Object with PureExceptionProfile {
    override protected val exceptionManager = testExceptionManager
    override protected val requestResponseBuilder = mockRequestResponseBuilder
  }

  describe("PureExceptionProfile") {
    describe("#onExceptionWithData") {
      it("should set a low-level exception request and stream its events") {
        val expected = Success(Pipeline.newPipeline(
          classOf[(ExceptionEvent, Seq[JDIEventDataResult])]
        ))
        val exceptionName = "full.exception.class.name"
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIRequestArgument])

        inSequence {
          expectCallAndInvokeRequestFunc[ExceptionEvent](
            mockRequestResponseBuilder,
            expected
          )

          mockSetExceptionFunc.expects(
            exceptionName,
            notifyCaught,
            notifyUncaught,
            arguments
          ).returning(Success(true)).once()
        }

        val actual = pureExceptionProfile.onExceptionWithData(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#onAllExceptionsWithData") {
      it("should set a low-level exception request and stream its events") {
        val expected = Success(Pipeline.newPipeline(
          classOf[(ExceptionEvent, Seq[JDIEventDataResult])]
        ))
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIRequestArgument])

        inSequence {
          expectCallAndInvokeRequestFunc[ExceptionEvent](
            mockRequestResponseBuilder,
            expected
          )

          mockSetCatchallExceptionFunc.expects(
            notifyCaught,
            notifyUncaught,
            arguments
          ).returning(Success(true)).once()
        }

        val actual = pureExceptionProfile.onAllExceptionsWithData(
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )

        actual should be (expected)
      }
    }
  }
}
