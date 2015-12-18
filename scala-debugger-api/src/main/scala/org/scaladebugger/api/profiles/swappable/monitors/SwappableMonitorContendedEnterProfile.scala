package org.senkbeil.debugger.api.profiles.swappable.monitors

import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.swappable.SwappableDebugProfile
import org.senkbeil.debugger.api.profiles.traits.monitors.MonitorContendedEnterProfile

import scala.util.Try

/**
 * Represents a swappable profile for monitor contended enter events that
 * redirects the invocation to another profile.
 */
trait SwappableMonitorContendedEnterProfile extends MonitorContendedEnterProfile {
  this: SwappableDebugProfile =>

  override def onMonitorContendedEnterWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorContendedEnterEventAndData]] = {
    withCurrentProfile.onMonitorContendedEnterWithData(extraArguments: _*)
  }
}
