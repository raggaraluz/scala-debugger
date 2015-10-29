package org.senkbeil.debugger.api.profiles.traits.monitors

import com.sun.jdi.event.MonitorWaitedEvent
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.Pipeline

/**
 * Represents the interface that needs to be implemented to provide
 * monitor waited functionality for a specific debug profile.
 */
trait MonitorWaitedProfile {
  /** Represents a monitor waited event and any associated data. */
  type MonitorWaitedEventAndData =
    (MonitorWaitedEvent, Seq[JDIEventDataResult])

  /**
   * Constructs a stream of monitor waited events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor waited events
   */
  def onMonitorWaited(
    extraArguments: JDIArgument*
  ): Pipeline[MonitorWaitedEvent, MonitorWaitedEvent] = {
    onMonitorWaitedWithData(extraArguments: _*).map(_._1).noop()
  }

  /**
   * Constructs a stream of monitor waited events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor waited events and any retrieved
   *         data based on requests from extra arguments
   */
  def onMonitorWaitedWithData(
    extraArguments: JDIArgument*
  ): Pipeline[MonitorWaitedEventAndData, MonitorWaitedEventAndData]
}
