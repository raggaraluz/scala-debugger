package org.scaladebugger.api.profiles.traits.monitors
import acyclic.file

import com.sun.jdi.event.MonitorContendedEnterEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.monitors.MonitorContendedEnterRequestInfo
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline

import scala.util.Try

/**
 * Represents the interface that needs to be implemented to provide
 * monitor contended enter functionality for a specific debug profile.
 */
trait MonitorContendedEnterProfile {
  /** Represents a monitor contended enter event and any associated data. */
  type MonitorContendedEnterEventAndData =
    (MonitorContendedEnterEvent, Seq[JDIEventDataResult])

  /**
   * Retrieves the collection of active and pending monitor contended enter
   * requests.
   *
   * @return The collection of information on monitor contended enter requests
   */
  def monitorContendedEnterRequests: Seq[MonitorContendedEnterRequestInfo]

  /**
   * Constructs a stream of monitor contended enter events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor contended enter events
   */
  def tryGetOrCreateMonitorContendedEnterRequest(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorContendedEnterEvent]] = {
    tryGetOrCreateMonitorContendedEnterRequestWithData(extraArguments: _*).map(_.map(_._1).noop())
  }

  /**
   * Constructs a stream of monitor contended enter events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor contended enter events and any retrieved
   *         data based on requests from extra arguments
   */
  def tryGetOrCreateMonitorContendedEnterRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorContendedEnterEventAndData]]

  /**
   * Constructs a stream of monitor contended enter events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor contended enter events
   */
  def getOrCreateMonitorContendedEnterRequest(
    extraArguments: JDIArgument*
  ): IdentityPipeline[MonitorContendedEnterEvent] = {
    tryGetOrCreateMonitorContendedEnterRequest(extraArguments: _*).get
  }

  /**
   * Constructs a stream of monitor contended enter events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor contended enter events and any retrieved
   *         data based on requests from extra arguments
   */
  def getOrCreateMonitorContendedEnterRequestWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[MonitorContendedEnterEventAndData] = {
    tryGetOrCreateMonitorContendedEnterRequestWithData(extraArguments: _*).get
  }
}
