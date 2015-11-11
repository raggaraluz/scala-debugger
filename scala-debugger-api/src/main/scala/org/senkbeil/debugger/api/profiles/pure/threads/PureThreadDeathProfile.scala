package org.senkbeil.debugger.api.profiles.pure.threads

import com.sun.jdi.event.ThreadDeathEvent
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.filters.UniqueIdPropertyFilter
import org.senkbeil.debugger.api.lowlevel.events.{JDIEventArgument, EventManager}
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.senkbeil.debugger.api.lowlevel.threads.ThreadDeathManager
import org.senkbeil.debugger.api.lowlevel.utils.JDIArgumentGroup
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.traits.threads.ThreadDeathProfile
import org.senkbeil.debugger.api.utils.Memoization
import org.senkbeil.debugger.api.lowlevel.events.EventType.ThreadDeathEventType

import scala.util.Try

/**
 * Represents a pure profile for thread death events that adds no
 * extra logic on top of the standard JDI.
 */
trait PureThreadDeathProfile extends ThreadDeathProfile {
  protected val threadDeathManager: ThreadDeathManager
  protected val eventManager: EventManager

  /**
   * Constructs a stream of thread death events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of thread death events and any retrieved data based on
   *         requests from extra arguments
   */
  override def onThreadDeathWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ThreadDeathEventAndData]] = Try {
    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(extraArguments: _*)
    val requestId = newThreadDeathRequest(rArgs)
    newThreadDeathPipeline(requestId, eArgs)
  }

  /**
   * Creates a new thread death request using the given arguments. The request
   * is memoized, meaning that the same request will be returned for the same
   * arguments. The memoized result will be thrown out if the underlying
   * request storage indicates that the request has been removed.
   *
   * @return The id of the created thread death request
   */
  protected val newThreadDeathRequest = {
    type Input = (Seq[JDIRequestArgument])
    type Key = (Seq[JDIRequestArgument])
    type Output = String

    new Memoization[Input, Key, Output](
      memoFunc = (input: Input) => {
        val requestId = newThreadDeathRequestId()
        val args = UniqueIdProperty(id = requestId) +: input

        threadDeathManager.createThreadDeathRequest(
          args: _*
        ).get

        requestId
      },
      cacheInvalidFunc = (key: Key) => {
        // TODO: Remove hard-coded filtering out of UniqueIdProperty,
        //       which shows up in saved arguments since it is passed in
        //       during the memoization above
        !threadDeathManager.threadDeathRequestList
          .flatMap(threadDeathManager.getThreadDeathRequestArguments)
          .map(_.filterNot(_.isInstanceOf[UniqueIdProperty]))
          .exists(_ == key)
      }
    )
  }

  /**
   * Creates a new pipeline of thread death events and data using the given
   * arguments. The pipeline is NOT memoized; therefore, each call creates a
   * new pipeline with a new underlying event handler feeding the pipeline.
   * This means that the pipeline needs to be properly closed to remove the
   * event handler.
   *
   * @param requestId The id of the request whose events to stream through the
   *                  new pipeline
   * @param args The additional event arguments to provide to the event handler
   *             feeding the new pipeline
   * @return The new thread death event and data pipeline
   */
  protected def newThreadDeathPipeline(
    requestId: String,
    args: Seq[JDIEventArgument]
  ): IdentityPipeline[ThreadDeathEventAndData] = {
    val eArgsWithFilter = UniqueIdPropertyFilter(id = requestId) +: args
    eventManager.addEventDataStream(ThreadDeathEventType, eArgsWithFilter: _*)
      .map(t => (t._1.asInstanceOf[ThreadDeathEvent], t._2)).noop()
  }

  /**
   * Used to generate new request ids to capture request/event matches.
   *
   * @return The new id as a string
   */
  protected def newThreadDeathRequestId(): String =
    java.util.UUID.randomUUID().toString
}
