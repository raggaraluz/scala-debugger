package org.scaladebugger.api.profiles.swappable.vm

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfile
import org.scaladebugger.api.profiles.traits.vm.VMDisconnectProfile

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
