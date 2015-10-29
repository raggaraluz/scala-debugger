package org.senkbeil.debugger.api.profiles.classes

import com.sun.jdi.event.ClassPrepareEvent
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.CloseablePipeline

/**
 * Represents the interface that needs to be implemented to provide
 * class prepare functionality for a specific debug profile.
 */
trait ClassPrepareProfile {
  /** Represents a class prepare event and any associated data. */
  type ClassPrepareEventAndData = (ClassPrepareEvent, Seq[JDIEventDataResult])

  /**
   * Constructs a stream of class prepare events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of class prepare events
   */
  def onClassPrepare(
    extraArguments: JDIArgument*
  ): CloseablePipeline[ClassPrepareEvent, ClassPrepareEvent]

  /**
   * Constructs a stream of class prepare events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of class prepare events and any retrieved data based on
   *         requests from extra arguments
   */
  def onClassPrepareWithData(
    extraArguments: JDIArgument*
  ): CloseablePipeline[ClassPrepareEventAndData, ClassPrepareEventAndData]
}
