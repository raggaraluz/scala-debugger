package org.scaladebugger.api.profiles.swappable.breakpoints

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfile
import org.scaladebugger.api.profiles.traits.breakpoints.BreakpointProfile

import scala.util.Try

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
  ): Try[IdentityPipeline[BreakpointEventAndData]] = {
    withCurrentProfile.onBreakpointWithData(
      fileName,
      lineNumber,
      extraArguments: _*
    )
  }
}
