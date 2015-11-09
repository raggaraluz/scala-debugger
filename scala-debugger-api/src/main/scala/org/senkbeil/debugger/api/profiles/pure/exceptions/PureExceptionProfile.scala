package org.senkbeil.debugger.api.profiles.pure.exceptions

import com.sun.jdi.event.ExceptionEvent
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.senkbeil.debugger.api.lowlevel.events.filters.UniqueIdPropertyFilter
import org.senkbeil.debugger.api.lowlevel.exceptions.ExceptionManager
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.senkbeil.debugger.api.lowlevel.utils.JDIArgumentGroup
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.traits.exceptions.ExceptionProfile
import org.senkbeil.debugger.api.utils.Memoization
import org.senkbeil.debugger.api.lowlevel.events.EventType._

import scala.util.Try

/**
 * Represents a pure profile for exceptions that adds no extra logic on
 * top of the standard JDI.
 */
trait PureExceptionProfile extends ExceptionProfile {
  protected val exceptionManager: ExceptionManager
  protected val eventManager: EventManager

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
  ): Try[IdentityPipeline[ExceptionEventAndData]] = Try {
    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(extraArguments: _*)
    val requestId = newCatchallExceptionRequest((
      notifyCaught,
      notifyUncaught,
      rArgs
      ))
    newExceptionPipeline(requestId, eArgs)
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
  ): Try[IdentityPipeline[ExceptionEventAndData]] = Try {
    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(extraArguments: _*)
    val requestId = newExceptionRequest((
      exceptionName,
      notifyCaught,
      notifyUncaught,
      rArgs
    ))
    newExceptionPipeline(requestId, eArgs)
  }

  /**
   * Creates a new exception request using the given arguments. The request is
   * memoized, meaning that the same request will be returned for the same
   * arguments. The memoized result will be thrown out if the underlying
   * request storage indicates that the request has been removed.
   *
   * @return The id of the created exception request
   */
  protected val newExceptionRequest = {
    type Input = (String, Boolean, Boolean, Seq[JDIRequestArgument])
    type Key = (String, Boolean, Boolean, Seq[JDIRequestArgument])
    type Output = String

    new Memoization[Input, Key, Output](
      memoFunc = (input: Input) => {
        val requestId = newExceptionRequestId()
        val args = UniqueIdProperty(id = requestId) +: input._4

        exceptionManager.createExceptionRequest(
          input._1,
          input._2,
          input._3,
          args: _*
        ).get

        requestId
      },
      cacheInvalidFunc = (key: Key) => {
        !exceptionManager.hasExceptionRequest(key._1)
      }
    )
  }

  /**
   * Creates a new catchall exception request using the given arguments. The
   * request is memoized, meaning that the same request will be returned for
   * the same arguments. The memoized result will be thrown out if the
   * underlying request storage indicates that the request has been removed.
   *
   * @return The id of the created exception request
   */
  protected val newCatchallExceptionRequest = {
    type Input = (Boolean, Boolean, Seq[JDIRequestArgument])
    type Key = (Boolean, Boolean, Seq[JDIRequestArgument])
    type Output = String

    new Memoization[Input, Key, Output](
      memoFunc = (input: Input) => {
        val requestId = newExceptionRequestId()
        val args = UniqueIdProperty(id = requestId) +: input._3

        exceptionManager.createCatchallExceptionRequest(
          input._1,
          input._2,
          args: _*
        ).get

        requestId
      },
      cacheInvalidFunc = (key: Key) => {
        !exceptionManager.hasCatchallExceptionRequest
      }
    )
  }

  /**
   * Creates a new pipeline of exception events and data using the given
   * arguments. The pipeline is NOT memoized; therefore, each call creates a
   * new pipeline with a new underlying event handler feeding the pipeline.
   * This means that the pipeline needs to be properly closed to remove the
   * event handler.
   *
   * @param requestId The id of the request whose events to stream through the
   *                  new pipeline
   * @param args The additional event arguments to provide to the event handler
   *             feeding the new pipeline
   * @return The new exception event and data pipeline
   */
  protected def newExceptionPipeline(
    requestId: String,
    args: Seq[JDIEventArgument]
  ): IdentityPipeline[ExceptionEventAndData] = {
    val eArgsWithFilter = UniqueIdPropertyFilter(id = requestId) +: args
    eventManager.addEventDataStream(ExceptionEventType, eArgsWithFilter: _*)
      .map(t => (t._1.asInstanceOf[ExceptionEvent], t._2)).noop()
  }

  /**
   * Used to generate new request ids to capture request/event matches.
   *
   * @return The new id as a string
   */
  protected def newExceptionRequestId(): String =
    java.util.UUID.randomUUID().toString
}
