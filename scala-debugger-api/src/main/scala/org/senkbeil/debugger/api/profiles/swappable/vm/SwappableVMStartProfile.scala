package org.senkbeil.debugger.api.profiles.swappable.vm

import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.swappable.SwappableDebugProfile
import org.senkbeil.debugger.api.profiles.traits.vm.VMStartProfile

/**
 * Represents a swappable profile for vm start events that redirects the
 * invocation to another profile.
 */
trait SwappableVMStartProfile extends VMStartProfile {
  this: SwappableDebugProfile =>

  override def onVMStartWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[VMStartEventAndData] = {
    withCurrentProfile.onVMStartWithData(extraArguments: _*)
  }
}
