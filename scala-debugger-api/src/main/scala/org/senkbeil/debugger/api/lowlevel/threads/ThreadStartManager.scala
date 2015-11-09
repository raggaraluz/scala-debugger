package org.senkbeil.debugger.api.lowlevel.threads

import java.util.concurrent.ConcurrentHashMap

import com.sun.jdi.request.{EventRequestManager, ThreadStartRequest}
import org.senkbeil.debugger.api.lowlevel.requests.Implicits._
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.senkbeil.debugger.api.utils.Logging

import scala.collection.JavaConverters._
import scala.util.Try

/**
 * Represents the manager for thread start requests.
 *
 * @param eventRequestManager The manager used to create thread start requests
 */
class ThreadStartManager(
  private val eventRequestManager: EventRequestManager
) extends Logging {
  type ThreadStartKey = String
  private val threadStartRequests = new ConcurrentHashMap[
    ThreadStartKey,
    (Seq[JDIRequestArgument], ThreadStartRequest)
  ]()

  /**
   * Retrieves the list of thread start requests contained by this manager.
   *
   * @return The collection of thread start requests in the form of
   *         (class name, method name)
   */
  def threadStartRequestList: Seq[ThreadStartKey] =
    threadStartRequests.keySet().asScala.toSeq

  /**
   * Sets the thread start request for the specified class and method.
   *
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createThreadStartRequest(
    extraArguments: JDIRequestArgument*
  ): Try[ThreadStartKey] = {
    val request = Try(eventRequestManager.createThreadStartRequest(
      Seq(
        EnabledProperty(value = true),
        SuspendPolicyProperty.EventThread
      ) ++ extraArguments: _*
    ))

    val id = newRequestId()
    if (request.isSuccess) {
      threadStartRequests.put(id, (extraArguments, request.get))
    }

    // If no exception was thrown, assume that we succeeded
    request.map(_ => id)
  }

  /**
   * Determines if a thread start request with the specified id.
   *
   * @param id The id of the Thread Start Request
   *
   * @return True if a thread start request with the id exists, otherwise false
   */
  def hasThreadStartRequest(id: ThreadStartKey): Boolean = {
    threadStartRequests.containsKey(id)
  }

  /**
   * Retrieves the thread start request using the specified id.
   *
   * @param id The id of the Thread Start Request
   *
   * @return Some thread start request if it exists, otherwise None
   */
  def getThreadStartRequest(id: ThreadStartKey): Option[ThreadStartRequest] = {
    Option(threadStartRequests.get(id)).map(_._2)
  }

  /**
   * Retrieves the arguments provided to the thread start request with the
   * specified id.
   *
   * @param id The id of the Thread Start Request
   *
   * @return Some collection of arguments if it exists, otherwise None
   */
  def getThreadStartRequestArguments(
    id: ThreadStartKey
  ): Option[Seq[JDIRequestArgument]] = {
    Option(threadStartRequests.get(id)).map(_._1)
  }

  /**
   * Removes the specified thread start request.
   *
   * @param id The id of the Thread Start Request
   *
   * @return True if the thread start request was removed (if it existed),
   *         otherwise false
   */
  def removeThreadStartRequest(id: ThreadStartKey): Boolean = {
    val request = Option(threadStartRequests.remove(id)).map(_._2)

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
