package org.senkbeil.debugger.api.profiles.pure.monitors

import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.traits.monitors.MonitorWaitProfile

import scala.util.Try

/**
 * Represents a pure profile for monitor wait events that adds no
 * extra logic on top of the standard JDI.
 */
trait PureMonitorWaitProfile extends MonitorWaitProfile {
  /**
   * Constructs a stream of monitor wait events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor wait events and any retrieved
   *         data based on requests from extra arguments
   */
  override def onMonitorWaitWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorWaitEventAndData]] = ???
}
