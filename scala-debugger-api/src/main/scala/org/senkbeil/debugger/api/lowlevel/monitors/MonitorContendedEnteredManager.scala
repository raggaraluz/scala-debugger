package org.senkbeil.debugger.api.lowlevel.monitors

import com.sun.jdi.request.{EventRequestManager, MonitorContendedEnteredRequest}
import org.senkbeil.debugger.api.lowlevel.requests.Implicits._
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.senkbeil.debugger.api.utils.{Logging, MultiMap}

import scala.util.Try

/**
 * Represents the manager for monitor contended entered requests.
 */
trait MonitorContendedEnteredManager {
  /**
   * Retrieves the list of monitor contended entered requests contained by
   * this manager.
   *
   * @return The collection of monitor contended entered requests in the form of
   *         ids
   */
  def monitorContendedEnteredRequestList: Seq[String]

  /**
   * Creates a new monitor contended entered request.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createMonitorContendedEnteredRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[String]

  /**
   * Creates a new monitor contended entered request.
   *
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createMonitorContendedEnteredRequest(
    extraArguments: JDIRequestArgument*
  ): Try[String]

  /**
   * Determines if a monitor contended entered request with the specified id.
   *
   * @param requestId The id of the Monitor Contended Entered Request
   *
   * @return True if a monitor contended entered request with the id exists,
   *         otherwise false
   */
  def hasMonitorContendedEnteredRequest(requestId: String): Boolean

  /**
   * Retrieves the monitor contended entered request using the specified id.
   *
   * @param requestId The id of the Monitor Contended Entered Request
   *
   * @return Some monitor contended entered request if it exists, otherwise None
   */
  def getMonitorContendedEnteredRequest(
    requestId: String
  ): Option[MonitorContendedEnteredRequest]

  /**
   * Retrieves the information for a monitor contended entered request with the
   * specified id.
   *
   * @param requestId The id of the Monitor Contended Entered Request
   *
   * @return Some information about the request if it exists, otherwise None
   */
  def getMonitorContendedEnteredRequestInfo(
    requestId: String
  ): Option[MonitorContendedEnteredRequestInfo]

  /**
   * Removes the specified monitor contended entered request.
   *
   * @param requestId The id of the Monitor Contended Entered Request
   *
   * @return True if the monitor contended entered request was removed
   *         (if it existed), otherwise false
   */
  def removeMonitorContendedEnteredRequest(
    requestId: String
  ): Boolean

  /**
   * Generates an id for a new request.
   *
   * @return The id as a string
   */
  protected def newRequestId(): String = java.util.UUID.randomUUID().toString
}
