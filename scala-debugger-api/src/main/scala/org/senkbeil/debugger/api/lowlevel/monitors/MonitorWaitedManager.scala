package org.senkbeil.debugger.api.lowlevel.monitors

import com.sun.jdi.request.MonitorWaitedRequest
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.Try

/**
 * Represents the manager for monitor waited requests.
 */
trait MonitorWaitedManager {
  /**
   * Retrieves the list of monitor waited requests contained by
   * this manager.
   *
   * @return The collection of monitor waited requests in the form of
   *         ids
   */
  def monitorWaitedRequestList: Seq[String]

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
  ): Try[String]

  /**
   * Creates a new monitor waited request.
   *
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createMonitorWaitedRequest(
    extraArguments: JDIRequestArgument*
  ): Try[String]

  /**
   * Determines if a monitor waited request with the specified id.
   *
   * @param id The id of the Monitor Waited Request
   *
   * @return True if a monitor waited request with the id exists,
   *         otherwise false
   */
  def hasMonitorWaitedRequest(id: String): Boolean

  /**
   * Retrieves the monitor waited request using the specified id.
   *
   * @param id The id of the Monitor Waited Request
   *
   * @return Some monitor waited request if it exists, otherwise None
   */
  def getMonitorWaitedRequest(id: String): Option[MonitorWaitedRequest]

  /**
   * Retrieves the information for a monitor waited request with the
   * specified id.
   *
   * @param id The id of the Monitor Waited Request
   *
   * @return Some information about the request if it exists, otherwise None
   */
  def getMonitorWaitedRequestInfo(id: String): Option[MonitorWaitedRequestInfo]

  /**
   * Removes the specified monitor waited request.
   *
   * @param id The id of the Monitor Waited Request
   *
   * @return True if the monitor waited request was removed
   *         (if it existed), otherwise false
   */
  def removeMonitorWaitedRequest(id: String): Boolean

  /**
   * Generates an id for a new request.
   *
   * @return The id as a string
   */
  protected def newRequestId(): String = java.util.UUID.randomUUID().toString
}
