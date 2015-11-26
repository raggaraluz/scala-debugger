package org.senkbeil.debugger.api.lowlevel.methods

import java.util.concurrent.ConcurrentHashMap

import com.sun.jdi.request.{EventRequestManager, MethodExitRequest}
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.filters.ClassInclusionFilter
import org.senkbeil.debugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.senkbeil.debugger.api.lowlevel.requests.Implicits._
import org.senkbeil.debugger.api.utils.Logging

import scala.collection.JavaConverters._
import scala.util.Try

/**
 * Represents the manager for method exit requests.
 *
 * @param eventRequestManager The manager used to create method exit requests
 */
class MethodExitManager(
  private val eventRequestManager: EventRequestManager
) extends Logging {
  /** The arguments used to lookup method exit requests: (Class, Method) */
  type MethodExitArgs = (String, String)

  /** The key used to lookup method exit requests */
  type MethodExitKey = String

  private val methodExitArgsToRequestId =
    new ConcurrentHashMap[MethodExitArgs, MethodExitKey]().asScala
  private val methodExitRequests =
    new ConcurrentHashMap[MethodExitKey, MethodExitRequest]().asScala

  /**
   * Retrieves the list of method exit requests contained by this manager.
   *
   * @return The collection of method exit requests in the form of
   *         (class name, method name)
   */
  def methodExitRequestList: Seq[MethodExitArgs] =
    methodExitArgsToRequestId.keySet.toSeq

  /**
   * Retrieves the list of method exit requests contained by this manager.
   *
   * @return The collection of method exit requests by id
   */
  def methodExitRequestListById: Seq[MethodExitKey] =
    methodExitRequests.keySet.toSeq

  /**
   * Creates a new method exit request for the specified class and method.
   *
   * @note The method name is purely used for indexing the request in the
   *       internal list. You should set a method name filter on the event
   *       handler for the method exit event.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param className The name of the class whose method exit events to watch
   * @param methodName The name of the method whose exit to watch
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createMethodExitRequestWithId(
    requestId: String,
    className: String,
    methodName: String,
    extraArguments: JDIRequestArgument*
  ): Try[MethodExitKey] = {
    val request = Try(eventRequestManager.createMethodExitRequest(
      Seq(
        ClassInclusionFilter(classPattern = className),
        EnabledProperty(value = true),
        SuspendPolicyProperty.EventThread
      ) ++ extraArguments: _*
    ))

    if (request.isSuccess) {
      methodExitArgsToRequestId.put((className, methodName), requestId)
      methodExitRequests.put(requestId, request.get)
    }

    // If no exception was thrown, assume that we succeeded
    request.map(_ => requestId)
  }

  /**
   * Creates a new method exit request for the specified class and method.
   *
   * @note The method name is purely used for indexing the request in the
   *       internal list. You should set a method name filter on the event
   *       handler for the method exit event.
   *
   * @param className The name of the class whose method exit events to watch
   * @param methodName The name of the method whose exit to watch
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createMethodExitRequest(
    className: String,
    methodName: String,
    extraArguments: JDIRequestArgument*
  ): Try[MethodExitKey] = {
    createMethodExitRequestWithId(
      newRequestId(),
      className,
      methodName,
      extraArguments: _*
    )
  }

  /**
   * Determines if a method exit request for the specific class and method
   * exists.
   *
   * @param className The name of the class targeted by the method exit request
   * @param methodName The name of the method targeted by the method exit
   *                   request
   *
   * @return True if a method exit request exists, otherwise false
   */
  def hasMethodExitRequest(className: String, methodName: String): Boolean = {
    methodExitArgsToRequestId
      .get((className, methodName))
      .exists(hasMethodExitRequestWithId)
  }

  /**
   * Determines if a method exit request exists with the specified id.
   *
   * @param requestId The id of the request
   *
   * @return True if a method exit request exists, otherwise false
   */
  def hasMethodExitRequestWithId(requestId: String): Boolean = {
    methodExitRequests.contains(requestId)
  }

  /**
   * Retrieves the method exit request for the specific class and method.
   *
   * @param className The name of the class targeted by the method exit request
   * @param methodName The name of the method targeted by the method exit
   *                   request
   *
   * @return Some method exit request if it exists, otherwise None
   */
  def getMethodExitRequest(
    className: String,
    methodName: String
  ): Option[MethodExitRequest] = {
    methodExitArgsToRequestId
      .get((className, methodName))
      .flatMap(getMethodExitRequestWithId)
  }

  /**
   * Retrieves the method exit request with the specified id.
   *
   * @param requestId The id of the request
   *
   * @return Some method exit request if it exists, otherwise None
   */
  def getMethodExitRequestWithId(
    requestId: String
  ): Option[MethodExitRequest] = {
    methodExitRequests.get(requestId)
  }

  /**
   * Removes the specified method exit request.
   *
   * @param className The name of the class targeted by the method exit request
   * @param methodName The name of the method targeted by the method exit
   *                   request
   *
   * @return True if the method exit request was removed (if it existed),
   *         otherwise false
   */
  def removeMethodExitRequest(
    className: String,
    methodName: String
  ): Boolean = {
    methodExitArgsToRequestId.get((className, methodName))
      .exists(removeMethodExitRequestWithId)
  }

  /**
   * Removes the specified method exit request.
   *
   * @param requestId The id of the request
   *
   * @return True if the method exit request was removed (if it existed),
   *         otherwise false
   */
  def removeMethodExitRequestWithId(
    requestId: String
  ): Boolean = {
    // Remove request with given id
    val request = methodExitRequests.remove(requestId)

    // Reverse-lookup arguments to remove argsToId mapping
    methodExitArgsToRequestId.find(_._2 == requestId).map(_._1)
      .foreach(methodExitArgsToRequestId.remove)

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
