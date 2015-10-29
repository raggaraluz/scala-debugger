package org.senkbeil.debugger.api.profiles.methods

import com.sun.jdi.event.MethodEntryEvent
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.CloseablePipeline

/**
 * Represents the interface that needs to be implemented to provide
 * method entry functionality for a specific debug profile.
 */
trait MethodEntryProfile {
  /** Represents a method entry event and any associated data. */
  type MethodEntryEventAndData = (MethodEntryEvent, Seq[JDIEventDataResult])

  /**
   * Constructs a stream of method entry events for the specified class and
   * method.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method to watch
   * @param methodName The name of the method to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of method entry events
   */
  def onMethodEntry(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): CloseablePipeline[MethodEntryEvent, MethodEntryEvent]

  /**
   * Constructs a stream of method entry events for the specified class and
   * method.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method to watch
   * @param methodName The name of the method to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of method entry events and any retrieved data based on
   *         requests from extra arguments
   */
  def onMethodEntryWithData(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): CloseablePipeline[MethodEntryEventAndData, MethodEntryEventAndData]
}
