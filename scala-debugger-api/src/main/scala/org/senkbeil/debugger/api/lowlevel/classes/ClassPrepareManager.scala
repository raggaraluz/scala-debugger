package org.senkbeil.debugger.api.lowlevel.classes

import com.sun.jdi.request.{ClassPrepareRequest, EventRequestManager}
import org.senkbeil.debugger.api.lowlevel.requests.Implicits._
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.senkbeil.debugger.api.utils.{Logging, MultiMap}

import scala.util.Try

/**
 * Represents the manager for class prepare requests.
 */
trait ClassPrepareManager {
  /**
   * Retrieves the list of class prepare requests contained by this manager.
   *
   * @return The collection of class prepare requests in the form of ids
   */
  def classPrepareRequestList: Seq[String]

  /**
   * Creates a new class prepare request.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createClassPrepareRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[String]

  /**
   * Creates a new class prepare request.
   *
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createClassPrepareRequest(
    extraArguments: JDIRequestArgument*
  ): Try[String]

  /**
   * Determines if a class prepare request with the specified id.
   *
   * @param id The id of the Class Prepare Request
   *
   * @return True if a class prepare request with the id exists, otherwise false
   */
  def hasClassPrepareRequest(id: String): Boolean

  /**
   * Retrieves the class prepare request using the specified id.
   *
   * @param id The id of the Class Prepare Request
   *
   * @return Some class prepare request if it exists, otherwise None
   */
  def getClassPrepareRequest(id: String): Option[ClassPrepareRequest]

  /**
   * Retrieves the information for a class prepare request with the
   * specified id.
   *
   * @param id The id of the Class Prepare Request
   *
   * @return Some information about the request if it exists, otherwise None
   */
  def getClassPrepareRequestInfo(
    id: String
  ): Option[ClassPrepareRequestInfo]

  /**
   * Removes the specified class prepare request.
   *
   * @param id The id of the Class Prepare Request
   *
   * @return True if the class prepare request was removed (if it existed),
   *         otherwise false
   */
  def removeClassPrepareRequest(id: String): Boolean

  /**
   * Generates an id for a new request.
   *
   * @return The id as a string
   */
  protected def newRequestId(): String =
    java.util.UUID.randomUUID().toString
}
