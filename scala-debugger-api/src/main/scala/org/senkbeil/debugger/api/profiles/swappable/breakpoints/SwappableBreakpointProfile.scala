package org.senkbeil.debugger.api.profiles.swappable.breakpoints

import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.swappable.SwappableDebugProfile
import org.senkbeil.debugger.api.profiles.traits.breakpoints.BreakpointProfile

/**
 * Represents a swappable profile for breakpoints that redirects the invocation
 * to another profile.
 */
trait SwappableBreakpointProfile extends BreakpointProfile {
  this: SwappableDebugProfile =>

  override def onBreakpointWithData(
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIArgument*
  ): IdentityPipeline[BreakpointEventAndData] = {
    withCurrentProfile.onBreakpointWithData(
      fileName,
      lineNumber,
      extraArguments: _*
    )
  }
}
