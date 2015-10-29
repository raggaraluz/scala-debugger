package org.senkbeil.debugger.api.profiles.monitors

import com.sun.jdi.event.MonitorWaitEvent
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.CloseablePipeline

/**
 * Represents the interface that needs to be implemented to provide
 * monitor wait functionality for a specific debug profile.
 */
trait MonitorWaitProfile {
  /** Represents a monitor wait event and any associated data. */
  type MonitorWaitEventAndData =
    (MonitorWaitEvent, Seq[JDIEventDataResult])

  /**
   * Constructs a stream of monitor wait events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor wait events
   */
  def onMonitorWait(
    extraArguments: JDIArgument*
  ): CloseablePipeline[MonitorWaitEvent, MonitorWaitEvent]

  /**
   * Constructs a stream of monitor wait events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor wait events and any retrieved
   *         data based on requests from extra arguments
   */
  def onMonitorWaitWithData(
    extraArguments: JDIArgument*
  ): CloseablePipeline[MonitorWaitEventAndData, MonitorWaitEventAndData]
}
