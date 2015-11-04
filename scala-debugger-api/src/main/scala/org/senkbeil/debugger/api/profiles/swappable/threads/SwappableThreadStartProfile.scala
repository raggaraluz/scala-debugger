package org.senkbeil.debugger.api.profiles.swappable.threads

import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.swappable.SwappableDebugProfile
import org.senkbeil.debugger.api.profiles.traits.threads.ThreadStartProfile

import scala.util.Try

/**
 * Represents a swappable profile for thread start events that redirects the
 * invocation to another profile.
 */
trait SwappableThreadStartProfile extends ThreadStartProfile {
  this: SwappableDebugProfile =>

  override def onThreadStartWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ThreadStartEventAndData]] = {
    withCurrentProfile.onThreadStartWithData(extraArguments: _*)
  }
}
