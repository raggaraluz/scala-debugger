package org.senkbeil.debugger.api.lowlevel.vm

import com.sun.jdi.request.{EventRequestManager, VMDeathRequest}
import org.senkbeil.debugger.api.lowlevel.requests.Implicits._
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.senkbeil.debugger.api.utils.{MultiMap, Logging}

import scala.util.Try

/**
 * Represents the manager for vm death requests.
 *
 * @param eventRequestManager The manager used to create vm death requests
 */
class VMDeathManager(
  private val eventRequestManager: EventRequestManager
) extends Logging {
  private val vmDeathRequests =
    new MultiMap[Seq[JDIRequestArgument], VMDeathRequest]

  /**
   * Retrieves the list of vm death requests contained by this manager.
   *
   * @return The collection of vm death requests in the form of ids
   */
  def vmDeathRequestList: Seq[String] = vmDeathRequests.ids

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
  ): Try[String] = {
    val request = Try(eventRequestManager.createVMDeathRequest(
      Seq(
        EnabledProperty(value = true),
        SuspendPolicyProperty.EventThread
      ) ++ extraArguments: _*
    ))

    if (request.isSuccess) vmDeathRequests.putWithId(
      requestId,
      extraArguments,
      request.get
    )

    // If no exception was thrown, assume that we succeeded
    request.map(_ => requestId)
  }

  /**
   * Creates a new vm death request for the specified class and method.
   *
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createVMDeathRequest(
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    createVMDeathRequestWithId(newRequestId(), extraArguments: _*)
  }

  /**
   * Determines if a vm death request with the specified id.
   *
   * @param id The id of the VM Death Request
   *
   * @return True if a vm death request with the id exists, otherwise false
   */
  def hasVMDeathRequest(id: String): Boolean = {
    vmDeathRequests.hasWithId(id)
  }

  /**
   * Retrieves the vm death request using the specified id.
   *
   * @param id The id of the VM Death Request
   *
   * @return Some vm death request if it exists, otherwise None
   */
  def getVMDeathRequest(id: String): Option[VMDeathRequest] = {
    vmDeathRequests.getWithId(id)
  }

  /**
   * Retrieves the arguments provided to the vm death request with the
   * specified id.
   *
   * @param id The id of the VM Death Request
   *
   * @return Some collection of arguments if it exists, otherwise None
   */
  def getVMDeathRequestArguments(
    id: String
  ): Option[Seq[JDIRequestArgument]] = {
    vmDeathRequests.getKeyWithId(id)
  }

  /**
   * Removes the specified vm death request.
   *
   * @param id The id of the VM Death Request
   *
   * @return True if the vm death request was removed (if it existed),
   *         otherwise false
   */
  def removeVMDeathRequest(id: String): Boolean = {
    val request = vmDeathRequests.removeWithId(id)

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

