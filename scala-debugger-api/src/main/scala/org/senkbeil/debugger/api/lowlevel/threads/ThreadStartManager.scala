package org.senkbeil.debugger.api.lowlevel.threads

import com.sun.jdi.request.{EventRequestManager, ThreadStartRequest}
import org.senkbeil.debugger.api.lowlevel.requests.Implicits._
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.senkbeil.debugger.api.utils.{MultiMap, Logging}

import scala.util.Try

/**
 * Represents the manager for thread start requests.
 *
 * @param eventRequestManager The manager used to create thread start requests
 */
class ThreadStartManager(
  private val eventRequestManager: EventRequestManager
) extends Logging {
  private val threadStartRequests =
    new MultiMap[Seq[JDIRequestArgument], ThreadStartRequest]

  /**
   * Retrieves the list of thread start requests contained by this manager.
   *
   * @return The collection of thread start requests in the form of ids
   */
  def threadStartRequestList: Seq[String] = threadStartRequests.ids

  /**
   * Creates a new thread start request for the specified class and method.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createThreadStartRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    val request = Try(eventRequestManager.createThreadStartRequest(
      Seq(
        EnabledProperty(value = true),
        SuspendPolicyProperty.EventThread
      ) ++ extraArguments: _*
    ))

    if (request.isSuccess) threadStartRequests.putWithId(
      requestId,
      extraArguments,
      request.get
    )

    // If no exception was thrown, assume that we succeeded
    request.map(_ => requestId)
  }

  /**
   * Creates a new thread start request for the specified class and method.
   *
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createThreadStartRequest(
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    createThreadStartRequestWithId(newRequestId(), extraArguments: _*)
  }

  /**
   * Determines if a thread start request with the specified id.
   *
   * @param id The id of the Thread Start Request
   *
   * @return True if a thread start request with the id exists, otherwise false
   */
  def hasThreadStartRequest(id: String): Boolean = {
    threadStartRequests.hasWithId(id)
  }

  /**
   * Retrieves the thread start request using the specified id.
   *
   * @param id The id of the Thread Start Request
   *
   * @return Some thread start request if it exists, otherwise None
   */
  def getThreadStartRequest(id: String): Option[ThreadStartRequest] = {
    threadStartRequests.getWithId(id)
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
    id: String
  ): Option[Seq[JDIRequestArgument]] = {
    threadStartRequests.getKeyWithId(id)
  }

  /**
   * Removes the specified thread start request.
   *
   * @param id The id of the Thread Start Request
   *
   * @return True if the thread start request was removed (if it existed),
   *         otherwise false
   */
  def removeThreadStartRequest(id: String): Boolean = {
    val request = threadStartRequests.removeWithId(id)

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

