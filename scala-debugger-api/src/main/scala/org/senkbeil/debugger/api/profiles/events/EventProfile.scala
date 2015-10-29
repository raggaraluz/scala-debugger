package org.senkbeil.debugger.api.profiles.events

import com.sun.jdi.event.Event
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.EventType.EventType
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.CloseablePipeline

/**
 * Represents the interface that needs to be implemented to provide
 * event functionality for a specific debug profile.
 */
trait EventProfile {
  /** Represents a breakpoint event and any associated data. */
  type EventAndData = (Event, Seq[JDIEventDataResult])

  /**
   * Constructs a stream of events for the specified event type.
   *
   * @param eventType The type of event to stream
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of events
   */
  def onEvent(
    eventType: EventType,
    extraArguments: JDIArgument*
  ): CloseablePipeline[Event, Event]

  /**
   * Constructs a stream of events for the specified event type.
   *
   * @param eventType The type of event to stream
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of events and any retrieved data based on
   *         requests from extra arguments
   */
  def onEventWithData(
    eventType: EventType,
    extraArguments: JDIArgument*
  ): CloseablePipeline[EventAndData, EventAndData]
}
