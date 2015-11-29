package org.senkbeil.debugger.api.lowlevel.monitors

import com.sun.jdi.request.{MonitorContendedEnterRequest, EventRequestManager}
import org.senkbeil.debugger.api.lowlevel.requests.Implicits._
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.senkbeil.debugger.api.utils.{MultiMap, Logging}

import scala.util.Try

/**
 * Represents the manager for monitor contended enter requests.
 *
 * @param eventRequestManager The manager used to create monitor contended
 *                            enter requests
 */
class MonitorContendedEnterManager(
  private val eventRequestManager: EventRequestManager
) extends Logging {
  private val monitorContendedEnterRequests =
    new MultiMap[Seq[JDIRequestArgument], MonitorContendedEnterRequest]

  /**
   * Retrieves the list of monitor contended enter requests contained by
   * this manager.
   *
   * @return The collection of monitor contended enter requests in the form of
   *         ids
   */
  def monitorContendedEnterRequestList: Seq[String] =
    monitorContendedEnterRequests.ids

  /**
   * Creates a new monitor contended enter request.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createMonitorContendedEnterRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    val request = Try(eventRequestManager.createMonitorContendedEnterRequest(
      Seq(
        EnabledProperty(value = true),
        SuspendPolicyProperty.EventThread
      ) ++ extraArguments: _*
    ))

    if (request.isSuccess) monitorContendedEnterRequests.putWithId(
      requestId,
      extraArguments,
      request.get
    )

    // If no exception was thrown, assume that we succeeded
    request.map(_ => requestId)
  }

  /**
   * Creates a new monitor contended enter request.
   *
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createMonitorContendedEnterRequest(
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    createMonitorContendedEnterRequestWithId(
      newRequestId(),
      extraArguments: _*
    )
  }

  /**
   * Determines if a monitor contended enter request with the specified id.
   *
   * @param id The id of the Monitor Contended Enter Request
   *
   * @return True if a monitor contended enter request with the id exists,
   *         otherwise false
   */
  def hasMonitorContendedEnterRequest(
    id: String
  ): Boolean = {
    monitorContendedEnterRequests.hasWithId(id)
  }

  /**
   * Retrieves the monitor contended enter request using the specified id.
   *
   * @param id The id of the Monitor Contended Enter Request
   *
   * @return Some monitor contended enter request if it exists, otherwise None
   */
  def getMonitorContendedEnterRequest(
    id: String
  ): Option[MonitorContendedEnterRequest] = {
    monitorContendedEnterRequests.getWithId(id)
  }

  /**
   * Retrieves the arguments provided to the monitor contended enter request
   * with the specified id.
   *
   * @param id The id of the Monitor Contended Enter Request
   *
   * @return Some collection of arguments if it exists, otherwise None
   */
  def getMonitorContendedEnterRequestArguments(
    id: String
  ): Option[Seq[JDIRequestArgument]] = {
    monitorContendedEnterRequests.getKeyWithId(id)
  }

  /**
   * Removes the specified monitor contended enter request.
   *
   * @param id The id of the Monitor Contended Enter Request
   *
   * @return True if the monitor contended enter request was removed
   *         (if it existed), otherwise false
   */
  def removeMonitorContendedEnterRequest(
    id: String
  ): Boolean = {
    val request = monitorContendedEnterRequests.removeWithId(id)

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

