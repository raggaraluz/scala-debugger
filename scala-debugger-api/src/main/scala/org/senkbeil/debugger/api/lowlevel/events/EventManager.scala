package org.senkbeil.debugger.api.lowlevel.events

import com.sun.jdi.event.{Event, EventQueue, EventSet}
import org.senkbeil.debugger.api.lowlevel.events.EventType._
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.utils.{Logging, LoopingTaskRunner, MultiMap}

/**
 * Represents a manager for events coming in from a virtual machine.
 */
trait EventManager {
  /**
   * Represents an event callback, receiving the event and returning whether or
   * not to resume.
   */
  type EventHandler = (Event, Seq[JDIEventDataResult]) => Boolean

  /**
   * Represents a JDI event and any associated data retrieved from it.
   */
  type EventAndData = (Event, Seq[JDIEventDataResult])

  /**
   * Indicates whether or not the event manager is processing events.
   *
   * @return True if it is running, otherwise false
   */
  def isRunning: Boolean

  /**
   * Begins the processing of events from the virtual machine.
   */
  def start(): Unit

  /**
   * Ends the processing of events from the virtual machine.
   */
  def stop(): Unit

  /**
   * Adds a new event stream that tracks incoming JDI events.
   *
   * @param eventType The type of JDI event to stream
   * @param eventArguments The arguments used when determining whether or not
   *                       to send the event down the stream
   *
   * @return The resulting event stream in the form of a pipeline of events
   */
  def addEventStream(
    eventType: EventType,
    eventArguments: JDIEventArgument*
  ): IdentityPipeline[Event]

  /**
   * Adds a new event data stream that tracks incoming JDI events and data
   * collected from those events.
   *
   * @param eventType The type of JDI event to stream
   * @param eventArguments The arguments used when determining whether or not
   *                       to send the event down the stream
   *
   * @return The resulting event stream in the form of a pipeline of events
   *         and collected data
   */
  def addEventDataStream(
    eventType: EventType,
    eventArguments: JDIEventArgument*
  ): IdentityPipeline[EventAndData]

  /**
   * Adds the event function to this manager. This event automatically counts
   * towards resuming the event set after completion.
   *
   * @param eventType The type of the event to add a function
   * @param eventHandler The function to add, taking the occurring event and
   *                     a collection of retrieved data from the event
   * @param eventArguments The arguments used when determining whether or not to
   *                       invoke the event handler
   *
   * @return The id associated with the event handler
   */
  def addResumingEventHandler(
    eventType: EventType,
    eventHandler: (Event, Seq[JDIEventDataResult]) => Unit,
    eventArguments: JDIEventArgument*
  ): String

  /**
   * Adds the event function to this manager. This event automatically counts
   * towards resuming the event set after completion.
   *
   * @param eventType The type of the event to add a function
   * @param eventHandler The function to add, taking the occurring event
   * @param eventArguments The arguments used when determining whether or not to
   *                       invoke the event handler
   *
   * @return The id associated with the event handler
   */
  def addResumingEventHandler(
    eventType: EventType,
    eventHandler: (Event) => Unit,
    eventArguments: JDIEventArgument*
  ): String

  /**
   * Adds the event function to this manager. The return value of the handler
   * function contributes towards whether or not to resume the event set.
   *
   * @param eventType The type of the event to add a function
   * @param eventHandler The function to add, taking the occurring event and
   *                     a collection of retrieved data from the event
   * @param eventArguments The arguments used when determining whether or not to
   *                       invoke the event handler
   *
   * @return The id associated with the event handler
   */
  def addEventHandler(
    eventType: EventType,
    eventHandler: EventHandler,
    eventArguments: JDIEventArgument*
  ): String

  /**
   * Adds the event function to this manager. The return value of the handler
   * function contributes towards whether or not to resume the event set.
   *
   * @param eventType The type of the event to add a function
   * @param eventHandler The function to add, taking the occurring event
   * @param eventArguments The arguments used when determining whether or not to
   *                       invoke the event handler
   *
   * @return The id associated with the event handler
   */
  def addEventHandler(
    eventType: EventType,
    eventHandler: (Event) => Boolean,
    eventArguments: JDIEventArgument*
  ): String

  /**
   * Retrieves the collection of event handler functions for the specific
   * event class.
   *
   * @param eventType The type of event whose functions to retrieve
   *
   * @return The collection of event functions
   */
  def getHandlersForEventType(eventType: EventType) : Seq[EventHandler]

  /**
   * Retrieves the collection of event handler functions for the specific
   * event class.
   *
   * @param eventType The type of event whose functions to retrieve
   *
   * @return The collection of event functions
   */
  def getHandlerIdsForEventType(eventType: EventType): Seq[String]

  /**
   * Retrieves the handler with the specified id.
   *
   * @param eventHandlerId The id of the handler to retrieve
   *
   * @return Some event handler if found, otherwise None
   */
  def getEventHandler(eventHandlerId: String): Option[EventHandler]

  /**
   * Removes the event function from this manager.
   *
   * @param eventHandlerId The id of the event handler to remove
   *
   * @return Some event handler if removed, otherwise None
   */
  def removeEventHandler(eventHandlerId: String): Option[EventHandler]

  /**
   * Generates an id for a new event handler.
   *
   * @return The id as a string
   */
  protected def newEventId(): String =
    java.util.UUID.randomUUID().toString
}
