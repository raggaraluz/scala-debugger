package org.senkbeil.debugger.api.lowlevel.watchpoints

import com.sun.jdi.request.{ModificationWatchpointRequest, EventRequestManager}
import org.senkbeil.debugger.api.lowlevel.classes.ClassManager
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.senkbeil.debugger.api.utils.{Logging, MultiMap}

import scala.collection.JavaConverters._
import scala.util.{Failure, Try}

/**
 * Represents the manager for modification watchpoint requests.
 *
 * @param eventRequestManager The manager used to create modification watchpoint
 *                            requests
 * @param classManager The manager used to retrieve information about classes
 *                     and their respective fields
 */
class ModificationWatchpointManager(
  private val eventRequestManager: EventRequestManager,
  private val classManager: ClassManager
) extends Logging {
  import org.senkbeil.debugger.api.lowlevel.requests.Implicits._

  /**
   * The arguments used to lookup modification watchpoint requests:
   * (Class name, field name)
   */
  type ModificationWatchpointArgs = (String, String)

  private val modificationWatchpointRequests =
    new MultiMap[ModificationWatchpointArgs, ModificationWatchpointRequest]

  /**
   * Retrieves the list of modification watchpoints contained by this manager.
   *
   * @return The collection of modification watchpoints in the form of fields
   */
  def modificationWatchpointRequestList: Seq[ModificationWatchpointArgs] =
    modificationWatchpointRequests.keys

  /**
   * Retrieves the list of modification watchpoints contained by this manager.
   *
   * @return The collection of modification watchpoints by id
   */
  def modificationWatchpointRequestListById: Seq[String] =
    modificationWatchpointRequests.ids

  /**
   * Creates a new modification watchpoint request for the specified field
   * using the field's name.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param className The name of the class containing the field
   * @param fieldName The name of the field to watch
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createModificationWatchpointRequestWithId(
    requestId: String,
    className: String,
    fieldName: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    val classReferenceType = classManager.allClasses.find(_.name() == className)
    val field = classReferenceType.flatMap(
      _.allFields().asScala.find(_.name() == fieldName)
    )

    if (field.isEmpty) return Failure(NoFieldFound(className, fieldName))

    val request = Try(eventRequestManager.createModificationWatchpointRequest(
      field.get,
      Seq(
        EnabledProperty(value = true),
        SuspendPolicyProperty.EventThread
      ) ++ extraArguments: _*
    ))

    if (request.isSuccess) modificationWatchpointRequests.putWithId(
      requestId,
      (className, fieldName),
      request.get
    )

    // If no exception was thrown, assume that we succeeded
    request.map(_ => requestId)
  }

  /**
   * Creates a new modification watchpoint request for the specified field
   * using the field's name.
   *
   * @param className The name of the class containing the field
   * @param fieldName The name of the field to watch
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createModificationWatchpointRequest(
    className: String,
    fieldName: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    createModificationWatchpointRequestWithId(
      newRequestId(),
      className,
      fieldName,
      extraArguments: _*
    )
  }

  /**
   * Determines if a modification watchpoint request with the specified field.
   *
   * @param className The name of the class containing the field
   * @param fieldName The name of the field to watch
   *
   * @return True if a modification watchpoint request with the id exists,
   *         otherwise false
   */
  def hasModificationWatchpointRequest(
    className: String,
    fieldName: String
  ): Boolean = {
    modificationWatchpointRequests.has((className, fieldName))
  }

  /**
   * Determines if a modification watchpoint request with the specified id.
   *
   * @param id The id of the Modification Watchpoint Request
   *
   * @return True if a modification watchpoint request with the id exists,
   *         otherwise false
   */
  def hasModificationWatchpointRequestWithId(id: String): Boolean = {
    modificationWatchpointRequests.hasWithId(id)
  }

  /**
   * Returns the collection of modification watchpoint requests representing the
   * modification watchpoint for the specified field.
   *
   * @param className The name of the class containing the field
   * @param fieldName The name of the field to watch
   *
   * @return Some collection of modification watchpoints for the field, or
   *         None if the specified field has no modification watchpoints
   */
  def getModificationWatchpointRequest(
    className: String,
    fieldName: String
  ): Option[Seq[ModificationWatchpointRequest]] = {
    modificationWatchpointRequests.get((className, fieldName))
  }

  /**
   * Retrieves the modification watchpoint request using the specified id.
   *
   * @param id The id of the Modification Watchpoint Request
   *
   * @return Some modification watchpoint request if it exists, otherwise None
   */
  def getModificationWatchpointRequestWithId(
    id: String
  ): Option[ModificationWatchpointRequest] = {
    modificationWatchpointRequests.getWithId(id)
  }

  /**
   * Removes the modification watchpoint for the specified field.
   *
   * @param className The name of the class containing the field
   * @param fieldName The name of the field to watch
   *
   * @return True if successfully removed modification watchpoint,
   *         otherwise false
   */
  def removeModificationWatchpointRequest(
    className: String,
    fieldName: String
  ): Boolean = {
    modificationWatchpointRequests.getIdsWithKey((className, fieldName))
      .exists(_.forall(removeModificationWatchpointRequestWithId))
  }

  /**
   * Removes the modification watchpoint request with the specified id.
   *
   * @param id The id of the Modification Watchpoint Request
   *
   * @return True if the modification watchpoint request was removed
   *         (if it existed), otherwise false
   */
  def removeModificationWatchpointRequestWithId(id: String): Boolean = {
    val request = modificationWatchpointRequests.removeWithId(id)

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

