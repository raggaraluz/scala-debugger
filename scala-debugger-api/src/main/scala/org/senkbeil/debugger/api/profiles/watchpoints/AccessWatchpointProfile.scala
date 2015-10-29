package org.senkbeil.debugger.api.profiles.watchpoints

import com.sun.jdi.event.AccessWatchpointEvent
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.CloseablePipeline

/**
 * Represents the interface that needs to be implemented to provide
 * access watchpoint functionality for a specific debug profile.
 */
trait AccessWatchpointProfile {
  /** Represents a access watchpoint event and any associated data. */
  type AccessWatchpointEventAndData =
    (AccessWatchpointEvent, Seq[JDIEventDataResult])

  /**
   * Constructs a stream of access watchpoint events for field in the specified
   * class.
   *
   * @param className The full name of the class whose field to watch
   * @param fieldName The name of the field to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of access watchpoint events
   */
  def onAccessWatchpoint(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): CloseablePipeline[AccessWatchpointEvent, AccessWatchpointEvent]

  /**
   * Constructs a stream of access watchpoint events for field in the specified
   * class.
   *
   * @param className The full name of the class whose field to watch
   * @param fieldName The name of the field to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of access watchpoint events and any retrieved data
   *         based on requests from extra arguments
   */
  def onAccessWatchpointWithData(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): CloseablePipeline[AccessWatchpointEventAndData, AccessWatchpointEventAndData]

  /**
   * Constructs a stream of access watchpoint events for the instance variable.
   *
   * @param instanceVarName The name of the instance variable to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of access watchpoint events
   */
  def onAccessWatchpoint(
    instanceVarName: String,
    extraArguments: JDIArgument*
  ): CloseablePipeline[AccessWatchpointEvent, AccessWatchpointEvent]

  /**
   * Constructs a stream of access watchpoint events for the instance variable.
   *
   * @param instanceVarName The name of the instance variable to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of access watchpoint events and any retrieved data
   *         based on requests from extra arguments
   */
  def onAccessWatchpointWithData(
    instanceVarName: String,
    extraArguments: JDIArgument*
  ): CloseablePipeline[AccessWatchpointEventAndData, AccessWatchpointEventAndData]
}
