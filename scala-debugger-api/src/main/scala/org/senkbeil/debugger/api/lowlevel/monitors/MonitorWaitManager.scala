package org.senkbeil.debugger.api.lowlevel.monitors

import java.util.concurrent.ConcurrentHashMap

import com.sun.jdi.request.{EventRequestManager, MonitorWaitRequest}
import org.senkbeil.debugger.api.lowlevel.requests.Implicits._
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.senkbeil.debugger.api.utils.Logging

import scala.collection.JavaConverters._
import scala.util.Try

/**
 * Represents the manager for monitor wait requests.
 *
 * @param eventRequestManager The manager used to create monitor wait requests
 */
class MonitorWaitManager(
  private val eventRequestManager: EventRequestManager
) extends Logging {
  type MonitorWaitKey = String
  private val monitorWaitRequests = new ConcurrentHashMap[
    MonitorWaitKey,
    (Seq[JDIRequestArgument], MonitorWaitRequest)
  ]()

  /**
   * Retrieves the list of monitor wait requests contained by
   * this manager.
   *
   * @return The collection of monitor wait requests in the form of ids
   */
  def monitorWaitRequestList: Seq[MonitorWaitKey] =
    monitorWaitRequests.keySet().asScala.toSeq

  /**
   * Creates a new monitor wait request.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createMonitorWaitRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[MonitorWaitKey] = {
    val request = Try(eventRequestManager.createMonitorWaitRequest(
      Seq(
        EnabledProperty(value = true),
        SuspendPolicyProperty.EventThread
      ) ++ extraArguments: _*
    ))

    if (request.isSuccess) {
      monitorWaitRequests.put(requestId, (extraArguments, request.get))
    }

    // If no exception was thrown, assume that we succeeded
    request.map(_ => requestId)
  }

  /**
   * Creates a new monitor wait request.
   *
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createMonitorWaitRequest(
    extraArguments: JDIRequestArgument*
  ): Try[MonitorWaitKey] = {
    createMonitorWaitRequestWithId(newRequestId(), extraArguments: _*)
  }

  /**
   * Determines if a monitor wait request with the specified id.
   *
   * @param id The id of the Monitor Wait Request
   *
   * @return True if a monitor wait request with the id exists,
   *         otherwise false
   */
  def hasMonitorWaitRequest(
    id: MonitorWaitKey
  ): Boolean = {
    monitorWaitRequests.containsKey(id)
  }

  /**
   * Retrieves the monitor wait request using the specified id.
   *
   * @param id The id of the Monitor Wait Request
   *
   * @return Some monitor wait request if it exists, otherwise None
   */
  def getMonitorWaitRequest(
    id: MonitorWaitKey
  ): Option[MonitorWaitRequest] = {
    Option(monitorWaitRequests.get(id)).map(_._2)
  }

  /**
   * Retrieves the arguments provided to the monitor wait request
   * with the specified id.
   *
   * @param id The id of the Thread Start Request
   *
   * @return Some collection of arguments if it exists, otherwise None
   */
  def getMonitorWaitRequestArguments(
    id: MonitorWaitKey
  ): Option[Seq[JDIRequestArgument]] = {
    Option(monitorWaitRequests.get(id)).map(_._1)
  }

  /**
   * Removes the specified monitor wait request.
   *
   * @param id The id of the Monitor Wait Request
   *
   * @return True if the monitor wait request was removed
   *         (if it existed), otherwise false
   */
  def removeMonitorWaitRequest(
    id: MonitorWaitKey
  ): Boolean = {
    val request = Option(monitorWaitRequests.remove(id)).map(_._2)

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
