package org.senkbeil.debugger.api.profiles.pure.events

import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.EventType.EventType
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.profiles.traits.events.EventProfile

/**
 * Represents a pure profile for events that adds no extra logic on
 * top of the standard JDI.
 */
trait PureEventProfile extends EventProfile {
  /**
   * Constructs a stream of events for the specified event type.
   *
   * @param eventType The type of event to stream
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of events and any retrieved data based on
   *         requests from extra arguments
   */
  override def onEventWithData(
    eventType: EventType,
    extraArguments: JDIArgument*
  ): Pipeline[EventAndData, EventAndData] = ???
}
