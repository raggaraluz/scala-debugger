package org.scaladebugger.api.profiles.swappable.monitors
import acyclic.file

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.monitors.MonitorContendedEnterRequestInfo
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfileManagement
import org.scaladebugger.api.profiles.traits.monitors.MonitorContendedEnterProfile

import scala.util.Try

/**
 * Represents a swappable profile for monitor contended enter events that
 * redirects the invocation to another profile.
 */
trait SwappableMonitorContendedEnterProfile extends MonitorContendedEnterProfile {
  this: SwappableDebugProfileManagement =>

  override def tryGetOrCreateMonitorContendedEnterRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorContendedEnterEventAndData]] = {
    withCurrentProfile.tryGetOrCreateMonitorContendedEnterRequestWithData(extraArguments: _*)
  }

  override def monitorContendedEnterRequests: Seq[MonitorContendedEnterRequestInfo] = {
    withCurrentProfile.monitorContendedEnterRequests
  }
}
