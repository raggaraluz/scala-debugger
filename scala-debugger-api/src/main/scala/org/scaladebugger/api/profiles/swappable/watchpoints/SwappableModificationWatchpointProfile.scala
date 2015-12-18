package org.scaladebugger.api.profiles.swappable.watchpoints

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfile
import org.scaladebugger.api.profiles.traits.watchpoints.ModificationWatchpointProfile

import scala.util.Try

/**
 * Represents a swappable profile for modification watchpoint events that
 * redirects the invocation to another profile.
 */
trait SwappableModificationWatchpointProfile extends ModificationWatchpointProfile {
  this: SwappableDebugProfile =>

  override def onModificationWatchpointWithData(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ModificationWatchpointEventAndData]] = {
    withCurrentProfile.onModificationWatchpointWithData(
      className,
      fieldName,
      extraArguments: _*
    )
  }
}
