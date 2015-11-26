package org.senkbeil.debugger.api.lowlevel.monitors

import java.util.concurrent.ConcurrentHashMap

import com.sun.jdi.request.{MonitorContendedEnteredRequest, EventRequestManager}
import org.senkbeil.debugger.api.lowlevel.requests.Implicits._
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.senkbeil.debugger.api.utils.Logging

import scala.collection.JavaConverters._
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
  type MonitorContendedEnteredKey = String
  private val monitorContendedEnteredRequests = new ConcurrentHashMap[
    MonitorContendedEnteredKey,
    (Seq[JDIRequestArgument], MonitorContendedEnteredRequest)
  ]()

  /**
   * Retrieves the list of monitor contended entered requests contained by
   * this manager.
   *
   * @return The collection of monitor contended entered requests in the form of
   *         ids
   */
  def monitorContendedEnteredRequestList: Seq[MonitorContendedEnteredKey] =
    monitorContendedEnteredRequests.keySet().asScala.toSeq

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
  ): Try[MonitorContendedEnteredKey] = {
    val request = Try(eventRequestManager.createMonitorContendedEnteredRequest(
      Seq(
        EnabledProperty(value = true),
        SuspendPolicyProperty.EventThread
      ) ++ extraArguments: _*
    ))

    if (request.isSuccess) {
      monitorContendedEnteredRequests.put(
        requestId,
        (extraArguments, request.get)
      )
    }

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
  ): Try[MonitorContendedEnteredKey] = {
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
    id: MonitorContendedEnteredKey
  ): Boolean = {
    monitorContendedEnteredRequests.containsKey(id)
  }

  /**
   * Retrieves the monitor contended entered request using the specified id.
   *
   * @param id The id of the Monitor Contended Entered Request
   *
   * @return Some monitor contended entered request if it exists, otherwise None
   */
  def getMonitorContendedEnteredRequest(
    id: MonitorContendedEnteredKey
  ): Option[MonitorContendedEnteredRequest] = {
    Option(monitorContendedEnteredRequests.get(id)).map(_._2)
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
    id: MonitorContendedEnteredKey
  ): Option[Seq[JDIRequestArgument]] = {
    Option(monitorContendedEnteredRequests.get(id)).map(_._1)
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
    id: MonitorContendedEnteredKey
  ): Boolean = {
    val request = Option(monitorContendedEnteredRequests.remove(id)).map(_._2)

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
