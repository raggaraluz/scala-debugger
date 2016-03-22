package org.scaladebugger.api.dsl.events

import com.sun.jdi.event.Event
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.EventType.EventType
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.events.EventProfile

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param eventProfile The profile to wrap
 */
class EventDSLWrapper private[dsl] (
  private val eventProfile: EventProfile
) {
  /** Represents a Event event and any associated data. */
  type EventEventAndData = (Event, Seq[JDIEventDataResult])

  /** @see EventProfile#tryCreateEventListener(EventType, JDIArgument*) */
  def onEvent(
    eventType: EventType,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[Event]] =
    eventProfile.tryCreateEventListener(eventType, extraArguments: _*)

  /** @see EventProfile#createEventListener(EventType, JDIArgument*) */
  def onUnsafeEvent(
    eventType: EventType,
    extraArguments: JDIArgument*
  ): IdentityPipeline[Event] =
    eventProfile.createEventListener(eventType, extraArguments: _*)

  /** @see EventProfile#createEventListenerWithData(EventType, JDIArgument*) */
  def onUnsafeEventWithData(
    eventType: EventType,
    extraArguments: JDIArgument*
  ): IdentityPipeline[EventEventAndData] =
    eventProfile.createEventListenerWithData(eventType, extraArguments: _*)

  /** @see EventProfile#tryCreateEventListenerWithData(EventType, JDIArgument*) */
  def onEventWithData(
    eventType: EventType,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[EventEventAndData]] =
    eventProfile.tryCreateEventListenerWithData(eventType, extraArguments: _*)
}
