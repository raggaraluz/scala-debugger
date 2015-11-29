package org.senkbeil.debugger.api.lowlevel.monitors

import com.sun.jdi.request.{MonitorWaitedRequest, EventRequestManager}
import org.senkbeil.debugger.api.lowlevel.requests.Implicits._
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.senkbeil.debugger.api.utils.{MultiMap, Logging}

import scala.util.Try

/**
 * Represents the manager for monitor waited requests.
 *
 * @param eventRequestManager The manager used to create monitor waited requests
 */
class MonitorWaitedManager(
  private val eventRequestManager: EventRequestManager
) extends Logging {
  private val monitorWaitedRequests =
    new MultiMap[Seq[JDIRequestArgument], MonitorWaitedRequest]

  /**
   * Retrieves the list of monitor waited requests contained by
   * this manager.
   *
   * @return The collection of monitor waited requests in the form of
   *         ids
   */
  def monitorWaitedRequestList: Seq[String] =
    monitorWaitedRequests.ids

  /**
   * Creates a new monitor waited request.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createMonitorWaitedRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    val request = Try(eventRequestManager.createMonitorWaitedRequest(
      Seq(
        EnabledProperty(value = true),
        SuspendPolicyProperty.EventThread
      ) ++ extraArguments: _*
    ))

    if (request.isSuccess) monitorWaitedRequests.putWithId(
      requestId,
      extraArguments,
      request.get
    )

    // If no exception was thrown, assume that we succeeded
    request.map(_ => requestId)
  }

  /**
   * Creates a new monitor waited request.
   *
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createMonitorWaitedRequest(
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    createMonitorWaitedRequestWithId(
      newRequestId(),
      extraArguments: _*
    )
  }

  /**
   * Determines if a monitor waited request with the specified id.
   *
   * @param id The id of the Monitor Waited Request
   *
   * @return True if a monitor waited request with the id exists,
   *         otherwise false
   */
  def hasMonitorWaitedRequest(
    id: String
  ): Boolean = {
    monitorWaitedRequests.hasWithId(id)
  }

  /**
   * Retrieves the monitor waited request using the specified id.
   *
   * @param id The id of the Monitor Waited Request
   *
   * @return Some monitor waited request if it exists, otherwise None
   */
  def getMonitorWaitedRequest(
    id: String
  ): Option[MonitorWaitedRequest] = {
    monitorWaitedRequests.getWithId(id)
  }

  /**
   * Retrieves the arguments provided to the monitor waited request
   * with the specified id.
   *
   * @param id The id of the Monitor Waited Request
   *
   * @return Some collection of arguments if it exists, otherwise None
   */
  def getMonitorWaitedRequestArguments(
    id: String
  ): Option[Seq[JDIRequestArgument]] = {
    monitorWaitedRequests.getKeyWithId(id)
  }

  /**
   * Removes the specified monitor waited request.
   *
   * @param id The id of the Monitor Waited Request
   *
   * @return True if the monitor waited request was removed
   *         (if it existed), otherwise false
   */
  def removeMonitorWaitedRequest(
    id: String
  ): Boolean = {
    val request = monitorWaitedRequests.removeWithId(id)

    request.foreach(eventRequestManager.deleteEventRequest)

    request.nonEmpty
  }

  /**
   * Generates an id for a new request.
   *
   * @return The id as a string
   */
  protected def newRequestId(): String = java.util.UUID.randomUUID().toString
}

