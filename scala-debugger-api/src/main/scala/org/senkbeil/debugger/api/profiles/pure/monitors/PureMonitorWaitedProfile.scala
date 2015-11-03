package org.senkbeil.debugger.api.profiles.pure.monitors

import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.traits.monitors.MonitorWaitedProfile

import scala.util.Try

/**
 * Represents a pure profile for monitor waited events that adds no
 * extra logic on top of the standard JDI.
 */
trait PureMonitorWaitedProfile extends MonitorWaitedProfile {
  /**
   * Constructs a stream of monitor waited events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor waited events and any retrieved
   *         data based on requests from extra arguments
   */
  override def onMonitorWaitedWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorWaitedEventAndData]] = ???
}
