package org.senkbeil.debugger.api.profiles.pure.classes

import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.traits.classes.ClassUnloadProfile

/**
 * Represents a pure profile for class unloading that adds no extra logic on
 * top of the standard JDI.
 */
trait PureClassUnloadProfile extends ClassUnloadProfile {
  /**
   * Constructs a stream of class unload events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of class unload events and any retrieved data based on
   *         requests from extra arguments
   */
  override def onClassUnloadWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[ClassUnloadEventAndData] = ???
}
