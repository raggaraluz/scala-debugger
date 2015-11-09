package org.senkbeil.debugger.api.profiles.pure.threads

import com.sun.jdi.event.ThreadStartEvent
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.senkbeil.debugger.api.lowlevel.events.filters.UniqueIdPropertyFilter
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.senkbeil.debugger.api.lowlevel.threads.ThreadStartManager
import org.senkbeil.debugger.api.lowlevel.utils.JDIArgumentGroup
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.traits.threads.ThreadStartProfile
import org.senkbeil.debugger.api.utils.Memoization
import org.senkbeil.debugger.api.lowlevel.events.EventType.ThreadStartEventType

import scala.util.Try

/**
 * Represents a pure profile for thread start events that adds no
 * extra logic on top of the standard JDI.
 */
trait PureThreadStartProfile extends ThreadStartProfile {
  protected val threadStartManager: ThreadStartManager
  protected val eventManager: EventManager

  /**
   * Constructs a stream of thread start events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of thread start events and any retrieved data based on
   *         requests from extra arguments
   */
  override def onThreadStartWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ThreadStartEventAndData]] = Try {
    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(extraArguments: _*)
    val requestId = newThreadStartRequest(rArgs)
    newThreadStartPipeline(requestId, eArgs)
  }

  /**
   * Creates a new thread start request using the given arguments. The request
   * is memoized, meaning that the same request will be returned for the same
   * arguments. The memoized result will be thrown out if the underlying
   * request storage indicates that the request has been removed.
   *
   * @return The id of the created thread start request
   */
  protected val newThreadStartRequest = {
    type Input = (Seq[JDIRequestArgument])
    type Key = (Seq[JDIRequestArgument])
    type Output = String

    new Memoization[Input, Key, Output](
      memoFunc = (input: Input) => {
        val requestId = newThreadStartRequestId()
        val args = UniqueIdProperty(id = requestId) +: input

        threadStartManager.createThreadStartRequest(
          args: _*
        ).get

        requestId
      },
      cacheInvalidFunc = (key: Key) => {
        !threadStartManager.threadStartRequestList
          .flatMap(threadStartManager.getThreadStartRequestArguments)
          .exists(_ == key)
      }
    )
  }

  /**
   * Creates a new pipeline of thread start events and data using the given
   * arguments. The pipeline is NOT memoized; therefore, each call creates a
   * new pipeline with a new underlying event handler feeding the pipeline.
   * This means that the pipeline needs to be properly closed to remove the
   * event handler.
   *
   * @param requestId The id of the request whose events to stream through the
   *                  new pipeline
   * @param args The additional event arguments to provide to the event handler
   *             feeding the new pipeline
   * @return The new thread start event and data pipeline
   */
  protected def newThreadStartPipeline(
    requestId: String,
    args: Seq[JDIEventArgument]
  ): IdentityPipeline[ThreadStartEventAndData] = {
    val eArgsWithFilter = UniqueIdPropertyFilter(id = requestId) +: args
    eventManager.addEventDataStream(ThreadStartEventType, eArgsWithFilter: _*)
      .map(t => (t._1.asInstanceOf[ThreadStartEvent], t._2)).noop()
  }

  /**
   * Used to generate new request ids to capture request/event matches.
   *
   * @return The new id as a string
   */
  protected def newThreadStartRequestId(): String =
    java.util.UUID.randomUUID().toString
}
