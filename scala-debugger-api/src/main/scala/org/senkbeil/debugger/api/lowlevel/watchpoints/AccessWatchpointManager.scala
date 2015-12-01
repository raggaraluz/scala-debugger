package org.senkbeil.debugger.api.lowlevel.watchpoints

import com.sun.jdi.Field
import com.sun.jdi.request.{AccessWatchpointRequest, EventRequestManager}
import org.senkbeil.debugger.api.lowlevel.classes.ClassManager
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.{SuspendPolicyProperty, EnabledProperty}
import org.senkbeil.debugger.api.utils.{MultiMap, Logging}

import scala.collection.JavaConverters._
import scala.util.{Failure, Try}

/**
 * Represents the manager for access watchpoint requests.
 *
 * @param eventRequestManager The manager used to create access watchpoint
 *                            requests
 */
class AccessWatchpointManager(
  private val eventRequestManager: EventRequestManager,
  private val classManager: ClassManager
) extends Logging {
  import org.senkbeil.debugger.api.lowlevel.requests.Implicits._

  /** The arguments used to lookup access watchpoint requests: (Field) */
  type AccessWatchpointArgs = (Field)

  private val accessWatchpointRequests =
    new MultiMap[AccessWatchpointArgs, AccessWatchpointRequest]

  /**
   * Retrieves the list of access watchpoints contained by this manager.
   *
   * @return The collection of access watchpoints in the form of fields
   */
  def accessWatchpointRequestList: Seq[AccessWatchpointArgs] =
    accessWatchpointRequests.keys

  /**
   * Retrieves the list of access watchpoints contained by this manager.
   *
   * @return The collection of access watchpoints by id
   */
  def accessWatchpointRequestListById: Seq[String] =
    accessWatchpointRequests.ids

  /**
   * Creates a new access watchpoint request for the specified field using the
   * field's name.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param className The name of the class containing the field
   * @param fieldName The name of the field to watch
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createAccessWatchpointRequestByNameWithId(
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

    createAccessWatchpointRequestWithId(
      requestId,
      field.get,
      extraArguments: _*
    )
  }

  /**
   * Creates a new access watchpoint request for the specified field using the
   * field's name.
   *
   * @param className The name of the class containing the field
   * @param fieldName The name of the field to watch
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createAccessWatchpointRequestByName(
    className: String,
    fieldName: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    createAccessWatchpointRequestByNameWithId(
      newRequestId(),
      className,
      fieldName,
      extraArguments: _*
    )
  }

  /**
   * Creates a new access watchpoint request for the specified field.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param field The field to watch for access
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createAccessWatchpointRequestWithId(
    requestId: String,
    field: Field,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    val request = Try(eventRequestManager.createAccessWatchpointRequest(
      field,
      Seq(
        EnabledProperty(value = true),
        SuspendPolicyProperty.EventThread
      ) ++ extraArguments: _*
    ))

    if (request.isSuccess) accessWatchpointRequests.putWithId(
      requestId,
      field,
      request.get
    )

    // If no exception was thrown, assume that we succeeded
    request.map(_ => requestId)
  }

  /**
   * Creates a new access watchpoint request for the specified field.
   *
   * @param field The field to watch for access
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createAccessWatchpointRequest(
    field: Field,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    createAccessWatchpointRequestWithId(
      newRequestId(),
      field,
      extraArguments: _*
    )
  }

  /**
   * Determines if a access watchpoint request with the specified field.
   *
   * @param field The field being watched for access
   *
   * @return True if a access watchpoint request with the id exists,
   *         otherwise false
   */
  def hasAccessWatchpointRequest(field: Field): Boolean = {
    accessWatchpointRequests.has(field)
  }

  /**
   * Determines if a access watchpoint request with the specified id.
   *
   * @param id The id of the Access Watchpoint Request
   *
   * @return True if a access watchpoint request with the id exists,
   *         otherwise false
   */
  def hasAccessWatchpointRequestWithId(id: String): Boolean = {
    accessWatchpointRequests.hasWithId(id)
  }

  /**
   * Returns the collection of access watchpoint requests representing the
   * access watchpoint for the specified field.
   *
   * @param field The field being watched for access
   *
   * @return Some collection of access watchpoints for the field, or None if
   *         the specified field has no access watchpoints
   */
  def getAccessWatchpointRequest(
    field: Field
  ): Option[Seq[AccessWatchpointRequest]] = {
    accessWatchpointRequests.get(field)
  }

  /**
   * Retrieves the access watchpoint request using the specified id.
   *
   * @param id The id of the Access Watchpoint Request
   *
   * @return Some access watchpoint request if it exists, otherwise None
   */
  def getAccessWatchpointRequestWithId(
    id: String
  ): Option[AccessWatchpointRequest] = {
    accessWatchpointRequests.getWithId(id)
  }

  /**
   * Removes the access watchpoint for the specified field.
   *
   * @param field The field being watched for access
   *
   * @return True if successfully removed access watchpoint, otherwise false
   */
  def removeAccessWatchpointRequest(
    field: Field
  ): Boolean = {
    accessWatchpointRequests.getIdsWithKey(field)
      .exists(_.forall(removeAccessWatchpointRequestWithId))
  }

  /**
   * Removes the access watchpoint request with the specified id.
   *
   * @param id The id of the Access Watchpoint Request
   *
   * @return True if the access watchpoint request was removed (if it existed),
   *         otherwise false
   */
  def removeAccessWatchpointRequestWithId(id: String): Boolean = {
    val request = accessWatchpointRequests.removeWithId(id)

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

