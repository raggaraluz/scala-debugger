package org.senkbeil.debugger.api.profiles.pure.classes

import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.traits.classes.ClassPrepareProfile

import scala.util.Try

/**
 * Represents a pure profile for class preparation that adds no extra logic on
 * top of the standard JDI.
 */
trait PureClassPrepareProfile extends ClassPrepareProfile {
  /**
   * Constructs a stream of class prepare events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of class prepare events and any retrieved data based on
   *         requests from extra arguments
   */
  override def onClassPrepareWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ClassPrepareEventAndData]] = ???
}
