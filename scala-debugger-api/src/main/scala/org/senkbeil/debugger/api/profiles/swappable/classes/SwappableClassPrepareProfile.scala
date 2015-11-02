package org.senkbeil.debugger.api.profiles.swappable.classes

import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.swappable.SwappableDebugProfile
import org.senkbeil.debugger.api.profiles.traits.classes.ClassPrepareProfile

/**
 * Represents a swappable profile for class prepare events that redirects the
 * invocation to another profile.
 */
trait SwappableClassPrepareProfile extends ClassPrepareProfile {
  this: SwappableDebugProfile =>

  override def onClassPrepareWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[ClassPrepareEventAndData] = {
    withCurrentProfile.onClassPrepareWithData(extraArguments: _*)
  }
}
