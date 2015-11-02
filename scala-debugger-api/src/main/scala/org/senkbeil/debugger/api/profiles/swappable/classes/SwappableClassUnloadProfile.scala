package org.senkbeil.debugger.api.profiles.swappable.classes

import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.swappable.SwappableDebugProfile
import org.senkbeil.debugger.api.profiles.traits.classes.ClassUnloadProfile

/**
 * Represents a swappable profile for class unload events that redirects the
 * invocation to another profile.
 */
trait SwappableClassUnloadProfile extends ClassUnloadProfile {
  this: SwappableDebugProfile =>

  override def onClassUnloadWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[ClassUnloadEventAndData] = {
    withCurrentProfile.onClassUnloadWithData(extraArguments: _*)
  }
}
