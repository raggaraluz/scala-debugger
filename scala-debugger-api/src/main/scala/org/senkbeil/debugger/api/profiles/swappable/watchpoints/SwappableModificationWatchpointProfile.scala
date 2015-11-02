package org.senkbeil.debugger.api.profiles.swappable.watchpoints

import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.swappable.SwappableDebugProfile
import org.senkbeil.debugger.api.profiles.traits.watchpoints.ModificationWatchpointProfile

/**
 * Represents a swappable profile for modification watchpoint events that
 * redirects the invocation to another profile.
 */
trait SwappableModificationWatchpointProfile extends ModificationWatchpointProfile {
  this: SwappableDebugProfile =>

  override def onModificationFieldWatchpointWithData(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): IdentityPipeline[ModificationWatchpointEventAndData] = {
    withCurrentProfile.onModificationFieldWatchpointWithData(
      className,
      fieldName,
      extraArguments: _*
    )
  }

  override def onModificationInstanceWatchpointWithData(
    instanceVarName: String,
    extraArguments: JDIArgument*
  ): IdentityPipeline[ModificationWatchpointEventAndData] = {
    withCurrentProfile.onModificationInstanceWatchpointWithData(
      instanceVarName,
      extraArguments: _*
    )
  }
}
