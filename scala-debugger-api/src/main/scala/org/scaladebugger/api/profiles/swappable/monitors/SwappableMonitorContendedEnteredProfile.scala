package org.senkbeil.debugger.api.profiles.swappable.monitors

import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.swappable.SwappableDebugProfile
import org.senkbeil.debugger.api.profiles.traits.monitors.MonitorContendedEnteredProfile

import scala.util.Try

/**
 * Represents a swappable profile for monitor contended entered events that
 * redirects the invocation to another profile.
 */
trait SwappableMonitorContendedEnteredProfile extends MonitorContendedEnteredProfile {
  this: SwappableDebugProfile =>

  override def onMonitorContendedEnteredWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorContendedEnteredEventAndData]] = {
    withCurrentProfile.onMonitorContendedEnteredWithData(extraArguments: _*)
  }
}
