package org.senkbeil.debugger.api.profiles.traits.monitors

import com.sun.jdi.event.MonitorContendedEnteredEvent
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline

/**
 * Represents the interface that needs to be implemented to provide
 * monitor contended entered functionality for a specific debug profile.
 */
trait MonitorContendedEnteredProfile {
  /** Represents a monitor contended entered event and any associated data. */
  type MonitorContendedEnteredEventAndData =
    (MonitorContendedEnteredEvent, Seq[JDIEventDataResult])

  /**
   * Constructs a stream of monitor contended entered events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor contended entered events
   */
  def onMonitorContendedEntered(
    extraArguments: JDIArgument*
  ): IdentityPipeline[MonitorContendedEnteredEvent] = {
    onMonitorContendedEnteredWithData(extraArguments: _*).map(_._1).noop()
  }

  /**
   * Constructs a stream of monitor contended entered events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor contended entered events and any retrieved
   *         data based on requests from extra arguments
   */
  def onMonitorContendedEnteredWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[MonitorContendedEnteredEventAndData]
}
