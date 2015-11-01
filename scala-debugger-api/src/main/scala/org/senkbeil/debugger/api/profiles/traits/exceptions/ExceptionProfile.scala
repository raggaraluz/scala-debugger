package org.senkbeil.debugger.api.profiles.traits.exceptions

import com.sun.jdi.event.ExceptionEvent
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.Pipeline

/**
 * Represents the interface that needs to be implemented to provide
 * exception functionality for a specific debug profile.
 */
trait ExceptionProfile {
  /** Represents a exception event and any associated data. */
  type ExceptionEventAndData = (ExceptionEvent, Seq[JDIEventDataResult])

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
   * @return The stream of exception events
   */
  def onException(
    exceptionName: String,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Pipeline[ExceptionEvent, ExceptionEvent] = {
    onExceptionWithData(
      exceptionName,
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    ).map(_._1).noop()
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
  def onExceptionWithData(
    exceptionName: String,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Pipeline[ExceptionEventAndData, ExceptionEventAndData]

  /**
   * Constructs a stream of exception events for all exceptions.
   *
   * @param notifyCaught If true, exception events will be streamed when an
   *                     exception is caught in a try/catch block
   * @param notifyUncaught If true, exception events will be streamed when an
   *                       exception is not caught in a try/catch block
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of exception events
   */
  def onAllExceptions(
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Pipeline[ExceptionEvent, ExceptionEvent] = {
    onAllExceptionsWithData(
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    ).map(_._1).noop()
  }

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
  def onAllExceptionsWithData(
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Pipeline[ExceptionEventAndData, ExceptionEventAndData]
}
