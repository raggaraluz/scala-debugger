package org.senkbeil.debugger.api.lowlevel.vm

import java.util.concurrent.ConcurrentHashMap

import com.sun.jdi.VirtualMachine
import com.sun.jdi.request.VMDeathRequest
import org.senkbeil.debugger.api.lowlevel.requests.Implicits._
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.filters.ClassInclusionFilter
import org.senkbeil.debugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.senkbeil.debugger.api.lowlevel.utils.JDIHelperMethods
import org.senkbeil.debugger.api.utils.Logging

import scala.collection.JavaConverters._
import scala.util.Try

/**
 * Represents the manager for vm death requests.
 *
 * @param _virtualMachine The virtual machine whose vm death requests to
 *                        manage
 */
class VMDeathManager(
  protected val _virtualMachine: VirtualMachine
) extends Logging {
  private val eventRequestManager = _virtualMachine.eventRequestManager()

  type VMDeathKey = String
  private val vmDeathRequests =
    new ConcurrentHashMap[VMDeathKey, VMDeathRequest]()

  /**
   * Retrieves the list of vm death requests contained by this manager.
   *
   * @return The collection of vm death requests in the form of
   *         (class name, method name)
   */
  def vmDeathList: Seq[VMDeathKey] =
    vmDeathRequests.keySet().asScala.toSeq

  /**
   * Sets the vm death request for the specified class and method.
   *
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def setVMDeath(
    extraArguments: JDIRequestArgument*
  ): Try[VMDeathKey] = {
    val request = Try(eventRequestManager.createVMDeathRequest(
      Seq(
        EnabledProperty(value = true),
        SuspendPolicyProperty.EventThread
      ) ++ extraArguments: _*
    ))

    val id = newRequestId()
    if (request.isSuccess) {
      vmDeathRequests.put(id, request.get)
    }

    // If no exception was thrown, assume that we succeeded
    request.map(_ => id)
  }

  /**
   * Determines if a vm death request with the specified id.
   *
   * @param id The id of the VM Death Request
   *
   * @return True if a vm death request with the id exists, otherwise false
   */
  def hasVMDeath(id: VMDeathKey): Boolean = {
    vmDeathRequests.containsKey(id)
  }

  /**
   * Retrieves the vm death request using the specified id.
   *
   * @param id The id of the VM Death Request
   *
   * @return Some vm death request if it exists, otherwise None
   */
  def getVMDeath(id: VMDeathKey): Option[VMDeathRequest] = {
    Option(vmDeathRequests.get(id))
  }

  /**
   * Removes the specified vm death request.
   *
   * @param id The id of the VM Death Request
   *
   * @return True if the vm death request was removed (if it existed),
   *         otherwise false
   */
  def removeVMDeath(id: VMDeathKey): Boolean = {
    val request = Option(vmDeathRequests.remove(id))

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
