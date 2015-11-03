package org.senkbeil.debugger.api.profiles.swappable.monitors

import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.swappable.SwappableDebugProfile
import org.senkbeil.debugger.api.profiles.traits.monitors.MonitorWaitedProfile

import scala.util.Try

/**
 * Represents a swappable profile for monitor waited events that
 * redirects the invocation to another profile.
 */
trait SwappableMonitorWaitedProfile extends MonitorWaitedProfile {
  this: SwappableDebugProfile =>

  override def onMonitorWaitedWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorWaitedEventAndData]] = {
    withCurrentProfile.onMonitorWaitedWithData(extraArguments: _*)
  }
}
