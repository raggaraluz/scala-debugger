package org.senkbeil.debugger.api.profiles.watchpoints

import com.sun.jdi.event.ModificationWatchpointEvent
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.CloseablePipeline

/**
 * Represents the interface that needs to be implemented to provide
 * modification watchpoint functionality for a specific debug profile.
 */
trait ModificationWatchpointProfile {
  /** Represents a modification watchpoint event and any associated data. */
  type ModificationWatchpointEventAndData =
    (ModificationWatchpointEvent, Seq[JDIEventDataResult])

  /**
   * Constructs a stream of modification watchpoint events for field in the
   * specified class.
   *
   * @param className The full name of the class whose field to watch
   * @param fieldName The name of the field to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of modification watchpoint events
   */
  def onModificationWatchpoint(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): CloseablePipeline[ModificationWatchpointEvent, ModificationWatchpointEvent]

  /**
   * Constructs a stream of modification watchpoint events for field in the
   * specified class.
   *
   * @param className The full name of the class whose field to watch
   * @param fieldName The name of the field to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of modification watchpoint events and any retrieved data
   *         based on requests from extra arguments
   */
  def onModificationWatchpointWithData(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): CloseablePipeline[ModificationWatchpointEventAndData, ModificationWatchpointEventAndData]

  /**
   * Constructs a stream of modification watchpoint events for the
   * instance variable.
   *
   * @param instanceVarName The name of the instance variable to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of modification watchpoint events
   */
  def onModificationWatchpoint(
    instanceVarName: String,
    extraArguments: JDIArgument*
  ): CloseablePipeline[ModificationWatchpointEvent, ModificationWatchpointEvent]

  /**
   * Constructs a stream of modification watchpoint events for the
   * instance variable.
   *
   * @param instanceVarName The name of the instance variable to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of modification watchpoint events and any retrieved data
   *         based on requests from extra arguments
   */
  def onModificationWatchpointWithData(
    instanceVarName: String,
    extraArguments: JDIArgument*
  ): CloseablePipeline[ModificationWatchpointEventAndData, ModificationWatchpointEventAndData]
}
