package org.senkbeil.debugger.events

import org.senkbeil.debugger.jdi.JDIHelperMethods
import org.senkbeil.utils.LogLike
import com.sun.jdi.VirtualMachine
import com.sun.jdi.event.Event

import java.util.concurrent.ConcurrentHashMap

import scala.util.{Failure, Success, Try}

import EventType._

/**
 * Represents a manager for events coming in from a virtual machine.
 *
 * @param _virtualMachine The virtual machine whose events to manage
 * @param loopingTaskRunner The runner used to process events
 * @param autoStart If true, starts the event processing automatically
 * @param startTaskRunner If true, will attempt to start the task runner if
 *                        not already started (upon starting the event manager)
 * @param onExceptionResume If true, any event handler that throws an exception
 *                          will count towards resuming the event set, otherwise
 *                          it will cause the event set to not resume
 */
class EventManager(
  protected val _virtualMachine: VirtualMachine,
  private val loopingTaskRunner: LoopingTaskRunner,
  private val autoStart: Boolean = true,
  private val startTaskRunner: Boolean = false,
  private val onExceptionResume: Boolean = true
) extends JDIHelperMethods with LogLike {
  /**
   * Represents an event callback, receiving the event and returning whether or
   * not to resume.
   */
  type EventFunction = (Event) => Boolean

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

      // Indicates whether to resume (true) based on the result of a function
      @inline def resumeOnResult(result: Try[Boolean]): Boolean = result match {
        case Success(r) => r
        case Failure(_) => onExceptionResume
      }

      // Flag used to indicate whether or not to resume the event set
      var resume = true

      // NOTE: Event sets are grouped into common events, so there is no need
      //       to worry about the resume flag being affected by different types
      //       of events
      while (eventSetIterator.hasNext) {
        val event = eventSetIterator.next()
        val eventType = EventType.eventToEventType(event)

        eventType.foreach(eType =>
          logger.trace(s"Processing event: ${eType.toString}"))

        // Execute all event functions for this event, collecting their results
        // as the flag for resuming
        resume &&= eventType.map(eventMap.get).map(events =>
          // If contains events, process each and get collective result
          if (events != null) events
            .map(func => Try(func(event)))
            .map(resumeOnResult)
            .reduce(_ && _)
          else true // No event to process, so just allow the flag to pass on
        ).forall(_ == true)
      }

      // Only resume if the consensus of the events says to do so
      if (resume) eventSet.resume()
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
   * Adds the event function to this manager. This event automatically counts
   * towards resuming the event set after completion.
   *
   * @param eventType The type of the event to add a function
   * @param eventFunction The function to add
   */
  def addResumingEventHandler(
    eventType: EventType,
    eventFunction: Event => Unit
  ): Unit = {
    // Convert the function to an "always true" event function
    val fullEventFunction = ((_: Unit) => true).compose(eventFunction)

    addEventHandler(eventType, fullEventFunction)
  }

  /**
   * Adds the event function to this manager. The return value of the handler
   * function contributes towards whether or not to resume the event set.
   *
   * @param eventType The type of the event to add a function
   * @param eventFunction The function to add
   */
  def addEventHandler(
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
