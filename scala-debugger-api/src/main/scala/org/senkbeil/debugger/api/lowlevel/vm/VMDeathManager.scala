package org.senkbeil.debugger.api.lowlevel.vm

import com.sun.jdi.request.{EventRequestManager, VMDeathRequest}
import org.senkbeil.debugger.api.lowlevel.requests.Implicits._
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.senkbeil.debugger.api.utils.{Logging, MultiMap}

import scala.util.Try

/**
 * Represents the manager for vm death requests.
 */
trait VMDeathManager {
  /**
   * Retrieves the list of vm death requests contained by this manager.
   *
   * @return The collection of vm death requests in the form of ids
   */
  def vmDeathRequestList: Seq[String]

  /**
   * Creates a new vm death request for the specified class and method.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createVMDeathRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[String]

  /**
   * Creates a new vm death request for the specified class and method.
   *
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createVMDeathRequest(extraArguments: JDIRequestArgument*): Try[String]

  /**
   * Determines if a vm death request with the specified id.
   *
   * @param id The id of the VM Death Request
   *
   * @return True if a vm death request with the id exists, otherwise false
   */
  def hasVMDeathRequest(id: String): Boolean

  /**
   * Retrieves the vm death request using the specified id.
   *
   * @param id The id of the VM Death Request
   *
   * @return Some vm death request if it exists, otherwise None
   */
  def getVMDeathRequest(id: String): Option[VMDeathRequest]

  /**
   * Retrieves the information for a vm death request with the specified id.
   *
   * @param id The id of the request
   *
   * @return Some vm death information if found, otherwise None
   */
  def getVMDeathRequestInfo(id: String): Option[VMDeathRequestInfo]

  /**
   * Removes the specified vm death request.
   *
   * @param id The id of the VM Death Request
   *
   * @return True if the vm death request was removed (if it existed),
   *         otherwise false
   */
  def removeVMDeathRequest(id: String): Boolean

  /**
   * Generates an id for a new request.
   *
   * @return The id as a string
   */
  protected def newRequestId(): String = java.util.UUID.randomUUID().toString
}

