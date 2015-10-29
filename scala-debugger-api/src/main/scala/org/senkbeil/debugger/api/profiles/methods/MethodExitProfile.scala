package org.senkbeil.debugger.api.profiles.methods

import com.sun.jdi.event.MethodExitEvent
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.CloseablePipeline

/**
 * Represents the interface that needs to be implemented to provide
 * method exit functionality for a specific debug profile.
 */
trait MethodExitProfile {
  /** Represents a method exit event and any associated data. */
  type MethodExitEventAndData = (MethodExitEvent, Seq[JDIEventDataResult])

  /**
   * Constructs a stream of method exit events for the specified class and
   * method.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method to watch
   * @param methodName The name of the method to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of method exit events
   */
  def onMethodExit(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): CloseablePipeline[MethodExitEvent, MethodExitEvent]

  /**
   * Constructs a stream of method exit events for the specified class and
   * method.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method to watch
   * @param methodName The name of the method to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of method exit events and any retrieved data based on
   *         requests from extra arguments
   */
  def onMethodExitWithData(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): CloseablePipeline[MethodExitEventAndData, MethodExitEventAndData]
}
