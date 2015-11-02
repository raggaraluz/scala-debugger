package org.senkbeil.debugger.api.profiles.swappable.monitors

import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.swappable.SwappableDebugProfile
import org.senkbeil.debugger.api.profiles.traits.monitors.MonitorWaitedProfile

/**
 * Represents a swappable profile for monitor waited events that
 * redirects the invocation to another profile.
 */
trait SwappableMonitorWaitedProfile extends MonitorWaitedProfile {
  this: SwappableDebugProfile =>

  override def onMonitorWaitedWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[MonitorWaitedEventAndData] = {
    withCurrentProfile.onMonitorWaitedWithData(extraArguments: _*)
  }
}
