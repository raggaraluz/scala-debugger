package org.senkbeil.debugger.api.profiles.pure.watchpoints

import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.traits.watchpoints.AccessWatchpointProfile

import scala.util.Try

/**
 * Represents a pure profile for access watchpoints that adds no
 * extra logic on top of the standard JDI.
 */
trait PureAccessWatchpointProfile extends AccessWatchpointProfile {
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
  override def onAccessFieldWatchpointWithData(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[AccessWatchpointEventAndData]] = ???

  /**
   * Constructs a stream of access watchpoint events for the instance variable.
   *
   * @param instanceVarName The name of the instance variable to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of access watchpoint events and any retrieved data
   *         based on requests from extra arguments
   */
  override def onAccessInstanceWatchpointWithData(
    instanceVarName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[AccessWatchpointEventAndData]] = ???
}
