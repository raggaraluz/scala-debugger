package org.senkbeil.debugger.api.profiles.traits.classes

import com.sun.jdi.event.ClassUnloadEvent
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.Pipeline

/**
 * Represents the interface that needs to be implemented to provide
 * class unload functionality for a specific debug profile.
 */
trait ClassUnloadProfile {
  /** Represents a class unload event and any associated data. */
  type ClassUnloadEventAndData = (ClassUnloadEvent, Seq[JDIEventDataResult])

  /**
   * Constructs a stream of class unload events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of class unload events
   */
  def onClassUnload(
    extraArguments: JDIArgument*
  ): Pipeline[ClassUnloadEvent, ClassUnloadEvent] = {
    onClassUnloadWithData(extraArguments: _*).map(_._1).noop()
  }

  /**
   * Constructs a stream of class unload events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of class unload events and any retrieved data based on
   *         requests from extra arguments
   */
  def onClassUnloadWithData(
    extraArguments: JDIArgument*
  ): Pipeline[ClassUnloadEventAndData, ClassUnloadEventAndData]
}
