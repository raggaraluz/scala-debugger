package org.senkbeil.debugger.api.profiles.swappable.methods

import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.swappable.SwappableDebugProfile
import org.senkbeil.debugger.api.profiles.traits.methods.MethodExitProfile

/**
 * Represents a swappable profile for method exit events that redirects the
 * invocation to another profile.
 */
trait SwappableMethodExitProfile extends MethodExitProfile {
  this: SwappableDebugProfile =>

  override def onMethodExitWithData(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): IdentityPipeline[MethodExitEventAndData] = {
    withCurrentProfile.onMethodExitWithData(
      className,
      methodName,
      extraArguments: _*
    )
  }
}
