package org.senkbeil.debugger.api.profiles.pure.monitors

import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.profiles.traits.monitors.MonitorContendedEnterProfile

/**
 * Represents a pure profile for monitor contended enter events that adds no
 * extra logic on top of the standard JDI.
 */
trait PureMonitorContendedEnterProfile extends MonitorContendedEnterProfile {
  /**
   * Constructs a stream of monitor contended enter events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor contended enter events and any retrieved
   *         data based on requests from extra arguments
   */
  override def onMonitorContendedEnterWithData(
    extraArguments: JDIArgument*
  ): Pipeline[MonitorContendedEnterEventAndData, MonitorContendedEnterEventAndData] = ???
}
