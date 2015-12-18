package org.senkbeil.debugger.api.profiles.swappable.vm

import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.swappable.SwappableDebugProfile
import org.senkbeil.debugger.api.profiles.traits.vm.VMDeathProfile

import scala.util.Try

/**
 * Represents a swappable profile for vm death events that redirects the
 * invocation to another profile.
 */
trait SwappableVMDeathProfile extends VMDeathProfile {
  this: SwappableDebugProfile =>

  override def onVMDeathWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[VMDeathEventAndData]] = {
    withCurrentProfile.onVMDeathWithData(extraArguments: _*)
  }
}
