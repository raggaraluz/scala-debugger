package org.senkbeil.debugger.api.profiles.swappable.watchpoints

import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.swappable.SwappableDebugProfile
import org.senkbeil.debugger.api.profiles.traits.watchpoints.AccessWatchpointProfile

import scala.util.Try

/**
 * Represents a swappable profile for access watchpoint events that redirects
 * the invocation to another profile.
 */
trait SwappableAccessWatchpointProfile extends AccessWatchpointProfile {
  this: SwappableDebugProfile =>

  override def onAccessWatchpointWithData(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[AccessWatchpointEventAndData]] = {
    withCurrentProfile.onAccessWatchpointWithData(
      className,
      fieldName,
      extraArguments: _*
    )
  }
}
