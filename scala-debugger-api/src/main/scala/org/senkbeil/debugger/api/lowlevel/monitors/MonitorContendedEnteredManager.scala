package org.senkbeil.debugger.api.lowlevel.monitors

import com.sun.jdi.request.{MonitorContendedEnteredRequest, EventRequestManager}
import org.senkbeil.debugger.api.lowlevel.requests.Implicits._
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.senkbeil.debugger.api.utils.{MultiMap, Logging}

import scala.util.Try

/**
 * Represents the manager for monitor contended entered requests.
 *
 * @param eventRequestManager The manager used to create monitor contended
 *                            entered requests
 */
class MonitorContendedEnteredManager(
  private val eventRequestManager: EventRequestManager
) extends Logging {
  private val monitorContendedEnteredRequests =
    new MultiMap[Seq[JDIRequestArgument], MonitorContendedEnteredRequest]

  /**
   * Retrieves the list of monitor contended entered requests contained by
   * this manager.
   *
   * @return The collection of monitor contended entered requests in the form of
   *         ids
   */
  def monitorContendedEnteredRequestList: Seq[String] =
    monitorContendedEnteredRequests.ids

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
  ): Try[String] = {
    val request = Try(eventRequestManager.createMonitorContendedEnteredRequest(
      Seq(
        EnabledProperty(value = true),
        SuspendPolicyProperty.EventThread
      ) ++ extraArguments: _*
    ))

    if (request.isSuccess) monitorContendedEnteredRequests.putWithId(
      requestId,
      extraArguments,
      request.get
    )

    // If no exception was thrown, assume that we succeeded
    request.map(_ => requestId)
  }

  /**
   * Creates a new monitor contended entered request.
   *
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createMonitorContendedEnteredRequest(
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    createMonitorContendedEnteredRequestWithId(
      newRequestId(),
      extraArguments: _*
    )
  }

  /**
   * Determines if a monitor contended entered request with the specified id.
   *
   * @param id The id of the Monitor Contended Entered Request
   *
   * @return True if a monitor contended entered request with the id exists,
   *         otherwise false
   */
  def hasMonitorContendedEnteredRequest(
    id: String
  ): Boolean = {
    monitorContendedEnteredRequests.hasWithId(id)
  }

  /**
   * Retrieves the monitor contended entered request using the specified id.
   *
   * @param id The id of the Monitor Contended Entered Request
   *
   * @return Some monitor contended entered request if it exists, otherwise None
   */
  def getMonitorContendedEnteredRequest(
    id: String
  ): Option[MonitorContendedEnteredRequest] = {
    monitorContendedEnteredRequests.getWithId(id)
  }

  /**
   * Retrieves the arguments provided to the monitor contended entered request
   * with the specified id.
   *
   * @param id The id of the Thread Start Request
   *
   * @return Some collection of arguments if it exists, otherwise None
   */
  def getMonitorContendedEnteredRequestArguments(
    id: String
  ): Option[Seq[JDIRequestArgument]] = {
    monitorContendedEnteredRequests.getKeyWithId(id)
  }

  /**
   * Removes the specified monitor contended entered request.
   *
   * @param id The id of the Monitor Contended Entered Request
   *
   * @return True if the monitor contended entered request was removed
   *         (if it existed), otherwise false
   */
  def removeMonitorContendedEnteredRequest(
    id: String
  ): Boolean = {
    val request = monitorContendedEnteredRequests.removeWithId(id)

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
