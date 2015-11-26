package org.senkbeil.debugger.api.profiles.pure.breakpoints

import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.breakpoints.{NoBreakpointLocationFound, BreakpointManager}
import org.senkbeil.debugger.api.lowlevel.events.{JDIEventArgument, EventManager}
import org.senkbeil.debugger.api.lowlevel.events.filters.UniqueIdPropertyFilter
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.senkbeil.debugger.api.lowlevel.utils.JDIArgumentGroup
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.traits.breakpoints.BreakpointProfile
import org.senkbeil.debugger.api.utils.Memoization
import org.senkbeil.debugger.api.lowlevel.events.EventType._
import com.sun.jdi.event._
import scala.util.Try

/**
 * Represents a pure profile for breakpoints that adds no extra logic on top
 * of the standard JDI.
 */
trait PureBreakpointProfile extends BreakpointProfile {
  protected val breakpointManager: BreakpointManager
  protected val eventManager: EventManager

  /**
   * Constructs a stream of breakpoint events for the specified file and line
   * number.
   *
   * @param fileName The name of the file where the breakpoint will be set
   * @param lineNumber The line number within the file where the breakpoint
   *                   will be set
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of breakpoint events and any retrieved data based on
   *         requests from extra arguments
   */
  override def onBreakpointWithData(
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[BreakpointEventAndData]] = Try {
    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(extraArguments: _*)
    val requestId = newBreakpointRequest((fileName, lineNumber, rArgs))
    newBreakpointPipeline(requestId, eArgs)
  }

  /**
   * Creates a new breakpoint request using the given arguments. The request is
   * memoized, meaning that the same request will be returned for the same
   * arguments. The memoized result will be thrown out if the underlying
   * request storage indicates that the request has been removed.
   *
   * @return The id of the created breakpoint request
   */
  protected val newBreakpointRequest = {
    type Input = (String, Int, Seq[JDIRequestArgument])
    type Key = (String, Int, Seq[JDIRequestArgument])
    type Output = String

    new Memoization[Input, Key, Output](
      memoFunc = (input: Input) => {
        val requestId = newBreakpointRequestId()
        val args = UniqueIdProperty(id = requestId) +: input._3

        // TODO: Handle pending breakpoint case in better way
        breakpointManager.createBreakpointRequest(
          input._1,
          input._2,
          args: _*
        ).failed.foreach {
          case _: NoBreakpointLocationFound => // Means breakpoint pending
          case t: Throwable => throw t // Unexpected error, so throw
        }

        requestId
      },
      cacheInvalidFunc = (key: Key) => {
        !breakpointManager.hasBreakpointRequest(key._1, key._2)
      }
    )
  }

  /**
   * Creates a new pipeline of breakpoint events and data using the given
   * arguments. The pipeline is NOT memoized; therefore, each call creates a
   * new pipeline with a new underlying event handler feeding the pipeline.
   * This means that the pipeline needs to be properly closed to remove the
   * event handler.
   *
   * @param requestId The id of the request whose events to stream through the
   *                  new pipeline
   * @param args The additional event arguments to provide to the event handler
   *             feeding the new pipeline
   * @return The new breakpoint event and data pipeline
   */
  protected def newBreakpointPipeline(
    requestId: String,
    args: Seq[JDIEventArgument]
  ): IdentityPipeline[BreakpointEventAndData] = {
    val eArgsWithFilter = UniqueIdPropertyFilter(id = requestId) +: args
    eventManager.addEventDataStream(BreakpointEventType, eArgsWithFilter: _*)
      .map(t => (t._1.asInstanceOf[BreakpointEvent], t._2)).noop()
  }

  /**
   * Used to generate new request ids to capture request/event matches.
   *
   * @return The new id as a string
   */
  protected def newBreakpointRequestId(): String =
    java.util.UUID.randomUUID().toString
}
