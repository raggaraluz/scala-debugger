package org.senkbeil.debugger.api.lowlevel.methods

import com.sun.jdi.request.{EventRequestManager, MethodExitRequest}
import org.senkbeil.debugger.api.lowlevel.requests.Implicits._
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.filters.ClassInclusionFilter
import org.senkbeil.debugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.senkbeil.debugger.api.utils.{MultiMap, Logging}

import scala.util.Try

/**
 * Represents the manager for method entry requests.
 *
 * @param eventRequestManager The manager used to create method entry requests
 */
class StandardMethodExitManager(
  private val eventRequestManager: EventRequestManager
) extends MethodExitManager with Logging {
  private val methodExitRequests =
    new MultiMap[MethodExitRequestInfo, MethodExitRequest]

  /**
   * Retrieves the list of method entry requests contained by this manager.
   *
   * @return The collection of method entry request information
   */
  override def methodExitRequestList: Seq[MethodExitRequestInfo] =
    methodExitRequests.keys

  /**
   * Retrieves the list of method entry requests contained by this manager.
   *
   * @return The collection of method entry requests by id
   */
  override def methodExitRequestListById: Seq[String] = methodExitRequests.ids

  /**
   * Creates a new method entry request for the specified class and method.
   *
   * @note The method name is purely used for indexing the request in the
   *       internal list. You should set a method name filter on the event
   *       handler for the method entry event.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param className The name of the class whose method entry events to watch
   * @param methodName The name of the method whose entry to watch
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  override def createMethodExitRequestWithId(
    requestId: String,
    className: String,
    methodName: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    val request = Try(eventRequestManager.createMethodExitRequest(
      Seq(
        ClassInclusionFilter(classPattern = className),
        EnabledProperty(value = true),
        SuspendPolicyProperty.EventThread
      ) ++ extraArguments: _*
    ))

    if (request.isSuccess) methodExitRequests.putWithId(
      requestId,
      MethodExitRequestInfo(requestId, className, methodName),
      request.get
    )

    // If no exception was thrown, assume that we succeeded
    request.map(_ => requestId)
  }

  /**
   * Creates a new method entry request for the specified class and method.
   *
   * @note The method name is purely used for indexing the request in the
   *       internal list. You should set a method name filter on the event
   *       handler for the method entry event.
   *
   * @param className The name of the class whose method entry events to watch
   * @param methodName The name of the method whose entry to watch
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  override def createMethodExitRequest(
    className: String,
    methodName: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    createMethodExitRequestWithId(
      newRequestId(),
      className,
      methodName,
      extraArguments: _*
    )
  }

  /**
   * Determines if a method entry request for the specific class and method
   * exists.
   *
   * @param className The name of the class targeted by the method entry request
   * @param methodName The name of the method targeted by the method entry
   *                   request
   *
   * @return True if a method entry request exists, otherwise false
   */
  override def hasMethodExitRequest(
    className: String,
    methodName: String
  ): Boolean = {
    methodExitRequests.hasWithKeyPredicate(m =>
      m.className == className && m.methodName == methodName
    )
  }

  /**
   * Determines if a method entry request exists with the specified id.
   *
   * @param requestId The id of the request
   *
   * @return True if a method entry request exists, otherwise false
   */
  override def hasMethodExitRequestWithId(requestId: String): Boolean = {
    methodExitRequests.hasWithId(requestId)
  }

  /**
   * Retrieves the method entry requests for the specific class and method.
   *
   * @param className The name of the class targeted by the method entry request
   * @param methodName The name of the method targeted by the method entry
   *                   request
   *
   * @return Some collection of method entry requests if they exist,
   *         otherwise None
   */
  override def getMethodExitRequest(
    className: String,
    methodName: String
  ): Option[Seq[MethodExitRequest]] = {
    val requests = methodExitRequests.getWithKeyPredicate(m =>
      m.className == className && m.methodName == methodName
    )

    if (requests.nonEmpty) Some(requests) else None
  }

  /**
   * Retrieves the method entry request with the specified id.
   *
   * @param requestId The id of the request
   *
   * @return Some method entry request if it exists, otherwise None
   */
  override def getMethodExitRequestWithId(
    requestId: String
  ): Option[MethodExitRequest] = {
    methodExitRequests.getWithId(requestId)
  }

  /**
   * Removes the specified method entry request.
   *
   * @param className The name of the class targeted by the method entry request
   * @param methodName The name of the method targeted by the method entry
   *                   request
   *
   * @return True if the method entry request was removed (if it existed),
   *         otherwise false
   */
  override def removeMethodExitRequest(
    className: String,
    methodName: String
  ): Boolean = {
    val ids = methodExitRequests.getIdsWithKeyPredicate(m =>
      m.className == className && m.methodName == methodName
    )

    ids.nonEmpty && ids.forall(removeMethodExitRequestWithId)
  }

  /**
   * Removes the specified method entry request.
   *
   * @param requestId The id of the request
   *
   * @return True if the method entry request was removed (if it existed),
   *         otherwise false
   */
  override def removeMethodExitRequestWithId(
    requestId: String
  ): Boolean = {
    // Remove request with given id
    val request = methodExitRequests.removeWithId(requestId)

    request.foreach(eventRequestManager.deleteEventRequest)

    request.nonEmpty
  }
}

