package org.senkbeil.debugger.api.profiles.traits.classes

import com.sun.jdi.event.ClassPrepareEvent
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.Pipeline

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
  ): Pipeline[ClassPrepareEvent, ClassPrepareEvent] = {
    onClassPrepareWithData(extraArguments: _*).map(_._1).noop()
  }

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
  ): Pipeline[ClassPrepareEventAndData, ClassPrepareEventAndData]
}
