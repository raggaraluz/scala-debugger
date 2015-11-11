package org.senkbeil.debugger.api.profiles.traits.events

import com.sun.jdi.event.Event
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.EventType.EventType
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline

import scala.util.Try

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
  ): Try[IdentityPipeline[Event]] = {
    onEventWithData(eventType, extraArguments: _*).map(_.map(_._1).noop())
  }

  /**
   * Constructs a stream of events for the specified event type.
   *
   * @param eventType The type of event to stream
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of events
   */
  def onUnsafeEvent(
    eventType: EventType,
    extraArguments: JDIArgument*
  ): IdentityPipeline[Event] = {
    onEvent(eventType, extraArguments: _*).get
  }

  /**
   * Constructs a stream of events for the specified event type.
   *
   * @param eventType The type of event to stream
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of events and any retrieved data based on
   *         requests from extra arguments
   */
  def onUnsafeEventWithData(
    eventType: EventType,
    extraArguments: JDIArgument*
  ): IdentityPipeline[EventAndData] = {
    onEventWithData(eventType, extraArguments: _*).get
  }

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
  ): Try[IdentityPipeline[EventAndData]]
}