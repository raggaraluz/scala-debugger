package com.senkbeil.debugger.events

import com.senkbeil.debugger.jdi.JDIHelperMethods
import com.senkbeil.utils.LogLike
import com.sun.jdi.VirtualMachine
import com.sun.jdi.event.Event

import java.util.concurrent.ConcurrentHashMap

import scala.util.Try

/**
 * Represents a manager for events coming in from a virtual machine.
 *
 * @param _virtualMachine The virtual machine whose events to manage
 * @param loopingTaskRunner The runner used to process events
 */
class EventManager(
  protected val _virtualMachine: VirtualMachine,
  private val loopingTaskRunner: LoopingTaskRunner
) extends JDIHelperMethods with LogLike {
  type EventFunction = (Event) => Unit
  private val eventMap =
    new ConcurrentHashMap[Class[_ <: Event], Seq[EventFunction]]()

  private var eventTaskId: String = _

  /**
   * Begins the processing of events from the virtual machine.
   */
  def start(): Unit = {
    require(eventTaskId == null, "Event manager already started!")

    logger.trace("Starting event manager for virtual machine!")
    eventTaskId = loopingTaskRunner.addTask {
      val eventQueue = _virtualMachine.eventQueue()
      val eventSet = eventQueue.remove()
      val eventSetIterator = eventSet.iterator()

      while (eventSetIterator.hasNext) {
        val event = eventSetIterator.next()
        val eventClass = event.getClass

        // Execute all event functions for this event
        Try(eventMap.get(eventClass)).foreach(
          _.foreach(func => Try(func(event))))
      }

      eventSet.resume()
    }
    logger.trace(s"Event process task: $eventTaskId")
  }

  /**
   * Ends the processing of events from the virtual machine.
   */
  def stop(): Unit = {
    require(eventTaskId != null, "Event manager not started!")

    logger.trace(s"Stopping event manager ($eventTaskId) for virtual machine!")
    loopingTaskRunner.removeTask(eventTaskId)
  }

  /**
   * Adds the event function to this manager.
   *
   * @param eventClass The class of the event to add a function
   * @param eventFunction The function to add
   * @tparam T The class of the event
   */
  def addEventHandler[T <: Event](
    eventClass: Class[T],
    eventFunction: EventFunction
  ): Unit = eventMap.synchronized {
    val oldEventFunctions =
      if (eventMap.contains(eventClass)) eventMap.get(eventClass)
      else Nil

    eventMap.put(eventClass, oldEventFunctions :+ eventFunction)
  }

  /**
   * Removes the event function from this manager.
   *
   * @param eventClass The class of the event whose function to remove
   * @param eventFunction The function to remove
   * @tparam T The class of the event
   */
  def removeEventHandler[T <: Event](
    eventClass: Class[T],
    eventFunction: EventFunction
  ): Unit = eventMap.synchronized {
    val oldEventFunctions =
      if (eventMap.contains(eventClass)) eventMap.get(eventClass)
      else Nil

    eventMap.put(eventClass, oldEventFunctions.filterNot(_ eq eventFunction))
  }
}
