package org.senkbeil.debugger.api.lowlevel.threads

import com.sun.jdi.request.ThreadStartRequest
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.Try

/**
 * Represents the manager for thread start requests.
 */
trait ThreadStartManager {
  /**
   * Retrieves the list of thread start requests contained by this manager.
   *
   * @return The collection of thread start requests in the form of ids
   */
  def threadStartRequestList: Seq[String]

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
  ): Try[String]

  /**
   * Creates a new thread start request for the specified class and method.
   *
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createThreadStartRequest(
    extraArguments: JDIRequestArgument*
  ): Try[String]

  /**
   * Determines if a thread start request with the specified id.
   *
   * @param id The id of the Thread Start Request
   *
   * @return True if a thread start request with the id exists, otherwise false
   */
  def hasThreadStartRequest(id: String): Boolean

  /**
   * Retrieves the thread start request using the specified id.
   *
   * @param id The id of the Thread Start Request
   *
   * @return Some thread start request if it exists, otherwise None
   */
  def getThreadStartRequest(id: String): Option[ThreadStartRequest]

  /**
   * Retrieves the information for a thread start request with the
   * specified id.
   *
   * @param id The id of the Thread Start Request
   *
   * @return Some information about the request if it exists, otherwise None
   */
  def getThreadStartRequestInfo(id: String): Option[ThreadStartRequestInfo]

  /**
   * Removes the specified thread start request.
   *
   * @param id The id of the Thread Start Request
   *
   * @return True if the thread start request was removed (if it existed),
   *         otherwise false
   */
  def removeThreadStartRequest(id: String): Boolean

  /**
   * Generates an id for a new request.
   *
   * @return The id as a string
   */
  protected def newRequestId(): String = java.util.UUID.randomUUID().toString
}
