package org.scaladebugger.api.profiles.traits.monitors
import acyclic.file

import com.sun.jdi.event.MonitorWaitedEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.monitors.MonitorWaitedRequestInfo
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline

import scala.util.Try

/**
 * Represents the interface that needs to be implemented to provide
 * monitor waited functionality for a specific debug profile.
 */
trait MonitorWaitedProfile {
  /** Represents a monitor waited event and any associated data. */
  type MonitorWaitedEventAndData =
    (MonitorWaitedEvent, Seq[JDIEventDataResult])

  /**
   * Retrieves the collection of active and pending monitor waited requests.
   *
   * @return The collection of information on monitor waited requests
   */
  def monitorWaitedRequests: Seq[MonitorWaitedRequestInfo]

  /**
   * Constructs a stream of monitor waited events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor waited events
   */
  def tryGetOrCreateMonitorWaitedRequest(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorWaitedEvent]] = {
    tryGetOrCreateMonitorWaitedRequestWithData(extraArguments: _*).map(_.map(_._1).noop())
  }

  /**
   * Constructs a stream of monitor waited events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor waited events and any retrieved
   *         data based on requests from extra arguments
   */
  def tryGetOrCreateMonitorWaitedRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorWaitedEventAndData]]

  /**
   * Constructs a stream of monitor waited events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor waited events
   */
  def getOrCreateMonitorWaitedRequest(
    extraArguments: JDIArgument*
  ): IdentityPipeline[MonitorWaitedEvent] = {
    tryGetOrCreateMonitorWaitedRequest(extraArguments: _*).get
  }

  /**
   * Constructs a stream of monitor waited events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor waited events and any retrieved
   *         data based on requests from extra arguments
   */
  def getOrCreateMonitorWaitedRequestWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[MonitorWaitedEventAndData] = {
    tryGetOrCreateMonitorWaitedRequestWithData(extraArguments: _*).get
  }
}
