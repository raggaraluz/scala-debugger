package org.senkbeil.debugger.api.profiles.traits.monitors

import com.sun.jdi.event.MonitorContendedEnterEvent
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.Pipeline

/**
 * Represents the interface that needs to be implemented to provide
 * monitor contended enter functionality for a specific debug profile.
 */
trait MonitorContendedEnterProfile {
  /** Represents a monitor contended enter event and any associated data. */
  type MonitorContendedEnterEventAndData =
    (MonitorContendedEnterEvent, Seq[JDIEventDataResult])

  /**
   * Constructs a stream of monitor contended enter events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor contended enter events
   */
  def onMonitorContendedEnter(
    extraArguments: JDIArgument*
  ): Pipeline[MonitorContendedEnterEvent, MonitorContendedEnterEvent] = {
    onMonitorContendedEnterWithData(extraArguments: _*).map(_._1).noop()
  }

  /**
   * Constructs a stream of monitor contended enter events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor contended enter events and any retrieved
   *         data based on requests from extra arguments
   */
  def onMonitorContendedEnterWithData(
    extraArguments: JDIArgument*
  ): Pipeline[MonitorContendedEnterEventAndData, MonitorContendedEnterEventAndData]
}
