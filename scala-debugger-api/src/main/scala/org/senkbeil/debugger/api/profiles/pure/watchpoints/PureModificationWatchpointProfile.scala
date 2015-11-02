package org.senkbeil.debugger.api.profiles.pure.watchpoints

import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.traits.watchpoints.ModificationWatchpointProfile

/**
 * Represents a pure profile for modification watchpoints that adds no
 * extra logic on top of the standard JDI.
 */
trait PureModificationWatchpointProfile extends ModificationWatchpointProfile {
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
  override def onModificationFieldWatchpointWithData(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): IdentityPipeline[ModificationWatchpointEventAndData] = ???

  /**
   * Constructs a stream of modification watchpoint events for the instance
   * variable.
   *
   * @param instanceVarName The name of the instance variable to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of modification watchpoint events and any retrieved data
   *         based on requests from extra arguments
   */
  override def onModificationInstanceWatchpointWithData(
    instanceVarName: String,
    extraArguments: JDIArgument*
  ): IdentityPipeline[ModificationWatchpointEventAndData] = ???
}
