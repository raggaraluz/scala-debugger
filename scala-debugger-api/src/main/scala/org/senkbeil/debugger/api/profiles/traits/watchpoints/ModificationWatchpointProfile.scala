package org.senkbeil.debugger.api.profiles.traits.watchpoints

import com.sun.jdi.event.ModificationWatchpointEvent
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.Pipeline

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
  def onModificationFieldWatchpoint(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Pipeline[ModificationWatchpointEvent, ModificationWatchpointEvent] = {
    onModificationFieldWatchpointWithData(
      className: String,
      fieldName: String,
      extraArguments: _*
    ).map(_._1).noop()
  }

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
  def onModificationFieldWatchpointWithData(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Pipeline[ModificationWatchpointEventAndData, ModificationWatchpointEventAndData]

  /**
   * Constructs a stream of modification watchpoint events for the
   * instance variable.
   *
   * @param instanceVarName The name of the instance variable to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of modification watchpoint events
   */
  def onModificationInstanceWatchpoint(
    instanceVarName: String,
    extraArguments: JDIArgument*
  ): Pipeline[ModificationWatchpointEvent, ModificationWatchpointEvent] = {
    onModificationInstanceWatchpointWithData(
      instanceVarName,
      extraArguments: _*
    ).map(_._1).noop()
  }

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
  def onModificationInstanceWatchpointWithData(
    instanceVarName: String,
    extraArguments: JDIArgument*
  ): Pipeline[ModificationWatchpointEventAndData, ModificationWatchpointEventAndData]
}
