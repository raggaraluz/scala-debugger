package org.senkbeil.debugger.api.lowlevel.monitors

import java.util.concurrent.ConcurrentHashMap

import com.sun.jdi.request.{EventRequestManager, MonitorWaitedRequest}
import org.senkbeil.debugger.api.lowlevel.requests.Implicits._
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.senkbeil.debugger.api.utils.Logging

import scala.collection.JavaConverters._
import scala.util.Try

/**
 * Represents the manager for monitor waited requests.
 *
 * @param eventRequestManager The manager used to create monitor waited requests
 */
class MonitorWaitedManager(
  private val eventRequestManager: EventRequestManager
) extends Logging {
  type MonitorWaitedKey = String
  private val monitorWaitedRequests = new ConcurrentHashMap[
    MonitorWaitedKey,
    (Seq[JDIRequestArgument], MonitorWaitedRequest)
  ]()

  /**
   * Retrieves the list of monitor waited requests contained by
   * this manager.
   *
   * @return The collection of monitor waited requests in the form of ids
   */
  def monitorWaitedRequestList: Seq[MonitorWaitedKey] =
    monitorWaitedRequests.keySet().asScala.toSeq

  /**
   * Creates a new monitor waited request.
   *
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createMonitorWaitedRequest(
    extraArguments: JDIRequestArgument*
  ): Try[MonitorWaitedKey] = {
    val request = Try(eventRequestManager.createMonitorWaitedRequest(
      Seq(
        EnabledProperty(value = true),
        SuspendPolicyProperty.EventThread
      ) ++ extraArguments: _*
    ))

    val id = newRequestId()
    if (request.isSuccess) {
      monitorWaitedRequests.put(id, (extraArguments, request.get))
    }

    // If no exception was thrown, assume that we succeeded
    request.map(_ => id)
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
    id: MonitorWaitedKey
  ): Boolean = {
    monitorWaitedRequests.containsKey(id)
  }

  /**
   * Retrieves the monitor waited request using the specified id.
   *
   * @param id The id of the Monitor Waited Request
   *
   * @return Some monitor waited request if it exists, otherwise None
   */
  def getMonitorWaitedRequest(
    id: MonitorWaitedKey
  ): Option[MonitorWaitedRequest] = {
    Option(monitorWaitedRequests.get(id)).map(_._2)
  }

  /**
   * Retrieves the arguments provided to the monitor waited request
   * with the specified id.
   *
   * @param id The id of the Thread Start Request
   *
   * @return Some collection of arguments if it exists, otherwise None
   */
  def getMonitorWaitedRequestArguments(
    id: MonitorWaitedKey
  ): Option[Seq[JDIRequestArgument]] = {
    Option(monitorWaitedRequests.get(id)).map(_._1)
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
    id: MonitorWaitedKey
  ): Boolean = {
    val request = Option(monitorWaitedRequests.remove(id)).map(_._2)

    request.foreach(eventRequestManager.deleteEventRequest)

    request.nonEmpty
  }

  /**
   * Generates an id for a new request.
   *
   * @return The id as a string
   */
  protected def newRequestId(): String =
    java.util.UUID.randomUUID().toString
}
