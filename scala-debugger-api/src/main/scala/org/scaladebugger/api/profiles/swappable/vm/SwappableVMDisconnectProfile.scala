package org.senkbeil.debugger.api.profiles.swappable.vm

import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.swappable.SwappableDebugProfile
import org.senkbeil.debugger.api.profiles.traits.vm.VMDisconnectProfile

import scala.util.Try

/**
 * Represents a swappable profile for vm disconnect events that redirects the
 * invocation to another profile.
 */
trait SwappableVMDisconnectProfile extends VMDisconnectProfile {
  this: SwappableDebugProfile =>

  override def onVMDisconnectWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[VMDisconnectEventAndData]] = {
    withCurrentProfile.onVMDisconnectWithData(extraArguments: _*)
  }
}
