package org.senkbeil.debugger.api.profiles.pure.monitors

import com.sun.jdi.event.MonitorContendedEnterEvent
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.senkbeil.debugger.api.lowlevel.events.filters.UniqueIdPropertyFilter
import org.senkbeil.debugger.api.lowlevel.monitors.MonitorContendedEnterManager
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.senkbeil.debugger.api.lowlevel.utils.JDIArgumentGroup
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.traits.monitors.MonitorContendedEnterProfile
import org.senkbeil.debugger.api.utils.Memoization
import org.senkbeil.debugger.api.lowlevel.events.EventType.MonitorContendedEnterEventType

import scala.util.Try

/**
 * Represents a pure profile for monitor contended enter events that adds no
 * extra logic on top of the standard JDI.
 */
trait PureMonitorContendedEnterProfile extends MonitorContendedEnterProfile {
  protected val monitorContendedEnterManager: MonitorContendedEnterManager
  protected val eventManager: EventManager

  /**
   * Constructs a stream of monitor contended enter events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor contended enter events and any retrieved
   *         data based on requests from extra arguments
   */
  override def onMonitorContendedEnterWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorContendedEnterEventAndData]] = Try {
    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(extraArguments: _*)
    val requestId = newMonitorContendedEnterRequest(rArgs)
    newMonitorContendedEnterPipeline(requestId, eArgs)
  }

  /**
   * Creates a new monitor contended enter request using the given arguments.
   * The request is memoized, meaning that the same request will be returned
   * for the same arguments. The memoized result will be thrown out if the
   * underlying request storage indicates that the request has been removed.
   *
   * @return The id of the created monitor contended enter request
   */
  protected val newMonitorContendedEnterRequest = {
    type Input = (Seq[JDIRequestArgument])
    type Key = (Seq[JDIRequestArgument])
    type Output = String

    new Memoization[Input, Key, Output](
      memoFunc = (input: Input) => {
        val requestId = newMonitorContendedEnterRequestId()
        val args = UniqueIdProperty(id = requestId) +: input

        monitorContendedEnterManager.createMonitorContendedEnterRequest(
          args: _*
        ).get

        requestId
      },
      cacheInvalidFunc = (key: Key) => {
        // TODO: Remove hard-coded filtering out of UniqueIdProperty,
        //       which shows up in saved arguments since it is passed in
        //       during the memoization above
        !monitorContendedEnterManager.monitorContendedEnterRequestList
          .flatMap(monitorContendedEnterManager.getMonitorContendedEnterRequestArguments)
          .map(_.filterNot(_.isInstanceOf[UniqueIdProperty]))
          .exists(_ == key)
      }
    )
  }

  /**
   * Creates a new pipeline of monitor contended enter events and data using
   * the given arguments. The pipeline is NOT memoized; therefore, each call
   * creates a new pipeline with a new underlying event handler feeding the
   * pipeline. This means that the pipeline needs to be properly closed to
   * remove the event handler.
   *
   * @param requestId The id of the request whose events to stream through the
   *                  new pipeline
   * @param args The additional event arguments to provide to the event handler
   *             feeding the new pipeline
   * @return The new monitor contended enter event and data pipeline
   */
  protected def newMonitorContendedEnterPipeline(
    requestId: String,
    args: Seq[JDIEventArgument]
    ): IdentityPipeline[MonitorContendedEnterEventAndData] = {
    val eArgsWithFilter = UniqueIdPropertyFilter(id = requestId) +: args
    eventManager.addEventDataStream(
      MonitorContendedEnterEventType,
      eArgsWithFilter: _*
    ).map(t => (t._1.asInstanceOf[MonitorContendedEnterEvent], t._2)).noop()
  }

  /**
   * Used to generate new request ids to capture request/event matches.
   *
   * @return The new id as a string
   */
  protected def newMonitorContendedEnterRequestId(): String =
    java.util.UUID.randomUUID().toString
}
