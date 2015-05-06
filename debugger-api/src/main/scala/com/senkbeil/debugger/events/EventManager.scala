package com.senkbeil.debugger.events

import com.senkbeil.debugger.jdi.JDIHelperMethods
import com.senkbeil.utils.LogLike
import com.sun.jdi.VirtualMachine
import com.sun.jdi.event.Event

import java.util.concurrent.ConcurrentHashMap

import scala.util.Try

import EventType._

/**
 * Represents a manager for events coming in from a virtual machine.
 *
 * @param _virtualMachine The virtual machine whose events to manage
 * @param loopingTaskRunner The runner used to process events
 * @param autoStart If true, starts the event processing automatically
 * @param startTaskRunner If true, will attempt to start the task runner if
 *                        not already started (upon starting the event manager)
 */
class EventManager(
  protected val _virtualMachine: VirtualMachine,
  private val loopingTaskRunner: LoopingTaskRunner,
  private val autoStart: Boolean = true,
  private val startTaskRunner: Boolean = false
) extends JDIHelperMethods with LogLike {
  type EventFunction = (Event) => Unit
  private val eventMap =
    new ConcurrentHashMap[EventType, Seq[EventFunction]]()

  private var eventTaskId: Option[String] = None

  /**
   * Indicates whether or not the event manager is processing events.
   *
   * @return True if it is running, otherwise false
   */
  def isRunning: Boolean = eventTaskId.nonEmpty

  /**
   * Begins the processing of events from the virtual machine.
   */
  def start(): Unit = {
    assert(!isRunning, "Event manager already started!")

    if (startTaskRunner && !loopingTaskRunner.isRunning) {
      logger.debug("Event manager starting looping task runner!")
      loopingTaskRunner.start()
    }

    logger.trace("Starting event manager for virtual machine!")
    eventTaskId = Some(loopingTaskRunner.addTask {
      val eventQueue = _virtualMachine.eventQueue()
      val eventSet = eventQueue.remove()
      val eventSetIterator = eventSet.iterator()

      while (eventSetIterator.hasNext) {
        val event = eventSetIterator.next()
        val eventType = EventType.eventToEventType(event)

        eventType.foreach(eType =>
          logger.trace(s"Processing event: ${eType.toString}"))

        // Execute all event functions for this event
        eventType.map(eventMap.get).foreach(events =>
          if (events != null) events.foreach(func => Try(func(event))))
      }

      eventSet.resume()
    })

    eventTaskId.foreach(id =>
      logger.trace(s"Event process task: $id"))
  }

  /**
   * Ends the processing of events from the virtual machine.
   */
  def stop(): Unit = {
    assert(isRunning, "Event manager not started!")

    logger.trace(s"Stopping event manager ($eventTaskId) for virtual machine!")
    loopingTaskRunner.removeTask(eventTaskId.get)
    eventTaskId = None
  }

  /**
   * Adds the event function to this manager.
   *
   * @param eventType The type of the event to add a function
   * @param eventFunction The function to add
   * @tparam T The class of the event
   */
  def addEventHandler[T <: Event](
    eventType: EventType,
    eventFunction: EventFunction
  ): Unit = eventMap.synchronized {
    val oldEventFunctions =
      if (eventMap.containsKey(eventType)) eventMap.get(eventType)
      else Nil

    eventMap.put(eventType, oldEventFunctions :+ eventFunction)
  }

  /**
   * Retrieves the collection of event handler functions for the specific
   * event class.
   *
   * @param eventType The type of event whose functions to retrieve
   * @tparam T The type associated with the class
   *
   * @return The collection of event functions
   */
  def getEventHandlers[T <: Event](eventType: EventType) : Seq[EventFunction] =
    if (eventMap.containsKey(eventType)) eventMap.get(eventType) else Nil

  /**
   * Removes the event function from this manager.
   *
   * @param eventType The event type whose function to remove
   * @param eventFunction The function to remove
   * @tparam T The class of the event
   */
  def removeEventHandler[T <: Event](
    eventType: EventType,
    eventFunction: EventFunction
  ): Unit = eventMap.synchronized {
    if (eventMap.containsKey(eventType)) {
      val oldEventFunctions = eventMap.get(eventType)
      eventMap.put(eventType, oldEventFunctions.filterNot(_ eq eventFunction))
    }
  }

  // ==========================================================================
  // = CONSTRUCTOR
  // ==========================================================================

  // If marked to start automatically, do so
  if (autoStart) start()
}
