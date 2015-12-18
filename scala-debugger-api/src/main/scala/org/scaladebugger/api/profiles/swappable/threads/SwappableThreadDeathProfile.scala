package org.senkbeil.debugger.api.profiles.swappable.threads

import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.swappable.SwappableDebugProfile
import org.senkbeil.debugger.api.profiles.traits.threads.ThreadDeathProfile

import scala.util.Try

/**
 * Represents a swappable profile for thread death events that redirects the
 * invocation to another profile.
 */
trait SwappableThreadDeathProfile extends ThreadDeathProfile {
  this: SwappableDebugProfile =>

  override def onThreadDeathWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ThreadDeathEventAndData]] = {
    withCurrentProfile.onThreadDeathWithData(extraArguments: _*)
  }
}
