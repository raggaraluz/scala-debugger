package org.senkbeil.debugger.api.profiles.swappable.monitors

import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.swappable.SwappableDebugProfile
import org.senkbeil.debugger.api.profiles.traits.monitors.MonitorContendedEnterProfile

/**
 * Represents a swappable profile for monitor contended enter events that
 * redirects the invocation to another profile.
 */
trait SwappableMonitorContendedEnterProfile extends MonitorContendedEnterProfile {
  this: SwappableDebugProfile =>

  override def onMonitorContendedEnterWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[MonitorContendedEnterEventAndData] = {
    withCurrentProfile.onMonitorContendedEnterWithData(extraArguments: _*)
  }
}
