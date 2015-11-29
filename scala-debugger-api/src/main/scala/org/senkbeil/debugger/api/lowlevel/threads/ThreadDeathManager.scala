package org.senkbeil.debugger.api.lowlevel.threads

import com.sun.jdi.request.{EventRequestManager, ThreadDeathRequest}
import org.senkbeil.debugger.api.lowlevel.requests.Implicits._
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.senkbeil.debugger.api.utils.{MultiMap, Logging}

import scala.util.Try

/**
 * Represents the manager for thread death requests.
 *
 * @param eventRequestManager The manager used to create thread death requests
 */
class ThreadDeathManager(
  private val eventRequestManager: EventRequestManager
) extends Logging {
  private val threadDeathRequests =
    new MultiMap[Seq[JDIRequestArgument], ThreadDeathRequest]

  /**
   * Retrieves the list of thread death requests contained by this manager.
   *
   * @return The collection of thread death requests in the form of ids
   */
  def threadDeathRequestList: Seq[String] = threadDeathRequests.ids

  /**
   * Creates a new thread death request for the specified class and method.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createThreadDeathRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    val request = Try(eventRequestManager.createThreadDeathRequest(
      Seq(
        EnabledProperty(value = true),
        SuspendPolicyProperty.EventThread
      ) ++ extraArguments: _*
    ))

    if (request.isSuccess) threadDeathRequests.putWithId(
      requestId,
      extraArguments,
      request.get
    )

    // If no exception was thrown, assume that we succeeded
    request.map(_ => requestId)
  }

  /**
   * Creates a new thread death request for the specified class and method.
   *
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createThreadDeathRequest(
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    createThreadDeathRequestWithId(newRequestId(), extraArguments: _*)
  }

  /**
   * Determines if a thread death request with the specified id.
   *
   * @param id The id of the Thread Death Request
   *
   * @return True if a thread death request with the id exists, otherwise false
   */
  def hasThreadDeathRequest(id: String): Boolean = {
    threadDeathRequests.hasWithId(id)
  }

  /**
   * Retrieves the thread death request using the specified id.
   *
   * @param id The id of the Thread Death Request
   *
   * @return Some thread death request if it exists, otherwise None
   */
  def getThreadDeathRequest(id: String): Option[ThreadDeathRequest] = {
    threadDeathRequests.getWithId(id)
  }

  /**
   * Retrieves the arguments provided to the thread death request with the
   * specified id.
   *
   * @param id The id of the Thread Death Request
   *
   * @return Some collection of arguments if it exists, otherwise None
   */
  def getThreadDeathRequestArguments(
    id: String
  ): Option[Seq[JDIRequestArgument]] = {
    threadDeathRequests.getKeyWithId(id)
  }

  /**
   * Removes the specified thread death request.
   *
   * @param id The id of the Thread Death Request
   *
   * @return True if the thread death request was removed (if it existed),
   *         otherwise false
   */
  def removeThreadDeathRequest(id: String): Boolean = {
    val request = threadDeathRequests.removeWithId(id)

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
