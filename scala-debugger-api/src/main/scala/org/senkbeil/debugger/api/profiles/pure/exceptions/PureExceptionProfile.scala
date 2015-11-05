package org.senkbeil.debugger.api.profiles.pure.exceptions

import com.sun.jdi.event.ExceptionEvent
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.exceptions.ExceptionManager
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.utils.JDIRequestResponseBuilder
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.traits.exceptions.ExceptionProfile

import scala.util.Try

/**
 * Represents a pure profile for exceptions that adds no extra logic on
 * top of the standard JDI.
 */
trait PureExceptionProfile extends ExceptionProfile {
  protected val exceptionManager: ExceptionManager
  protected val requestResponseBuilder: JDIRequestResponseBuilder

  /**
   * Constructs a stream of exception events for all exceptions.
   *
   * @param notifyCaught If true, exception events will be streamed when an
   *                     exception is caught in a try/catch block
   * @param notifyUncaught If true, exception events will be streamed when an
   *                       exception is not caught in a try/catch block
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of exception events and any retrieved data based on
   *         requests from extra arguments
   */
  override def onAllExceptionsWithData(
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ExceptionEventAndData]] = {
    /** Creates a new request using arguments. */
    def newRequest(args: Seq[JDIRequestArgument]): Unit = {
      // Ignore true/false, but propagate up any errors
      exceptionManager.setCatchallException(
        notifyCaught,
        notifyUncaught,
        args: _*
      )
    }

    requestResponseBuilder.buildRequestResponse[ExceptionEvent](
      newRequest,
      extraArguments: _*
    )
  }

  /**
   * Constructs a stream of exception events for the specified exception.
   *
   * @param exceptionName The full class name of the exception
   * @param notifyCaught If true, exception events will be streamed when the
   *                     exception is caught in a try/catch block
   * @param notifyUncaught If true, exception events will be streamed when the
   *                       exception is not caught in a try/catch block
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of exception events and any retrieved data based on
   *         requests from extra arguments
   */
  override def onExceptionWithData(
    exceptionName: String,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ExceptionEventAndData]] = {
    /** Creates a new request using arguments. */
    def newRequest(args: Seq[JDIRequestArgument]): Unit = {
      // Ignore true/false, but propagate up any errors
      exceptionManager.setException(
        exceptionName,
        notifyCaught,
        notifyUncaught,
        args: _*
      )
    }

    requestResponseBuilder.buildRequestResponse[ExceptionEvent](
      newRequest,
      extraArguments: _*
    )
  }
}
