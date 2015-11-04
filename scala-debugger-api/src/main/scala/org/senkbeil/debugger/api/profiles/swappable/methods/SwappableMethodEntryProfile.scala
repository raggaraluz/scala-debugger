package org.senkbeil.debugger.api.profiles.swappable.methods

import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.swappable.SwappableDebugProfile
import org.senkbeil.debugger.api.profiles.traits.methods.MethodEntryProfile

import scala.util.Try

/**
 * Represents a swappable profile for method entry events that redirects the
 * invocation to another profile.
 */
trait SwappableMethodEntryProfile extends MethodEntryProfile {
  this: SwappableDebugProfile =>

  override def onMethodEntryWithData(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MethodEntryEventAndData]] = {
    withCurrentProfile.onMethodEntryWithData(
      className,
      methodName,
      extraArguments: _*
    )
  }
}
