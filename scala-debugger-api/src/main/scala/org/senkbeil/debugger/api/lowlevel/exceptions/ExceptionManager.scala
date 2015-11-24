package org.senkbeil.debugger.api.lowlevel.exceptions

import java.util.concurrent.ConcurrentHashMap

import com.sun.jdi.{ReferenceType, VirtualMachine}
import com.sun.jdi.request.{EventRequestManager, ExceptionRequest}
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.{SuspendPolicyProperty, EnabledProperty}
import org.senkbeil.debugger.api.utils.Logging
import scala.collection.JavaConverters._
import org.senkbeil.debugger.api.lowlevel.requests.Implicits._

import scala.util.{Failure, Try}

/**
 * Represents the manager for exception requests.
 *
 * @param virtualMachine The virtual machine whose classes related to
 *                        exceptions to retrieve
 * @param eventRequestManager The manager used to create exception requests
 */
class ExceptionManager(
  private val virtualMachine: VirtualMachine,
  private val eventRequestManager: EventRequestManager
) extends Logging {
  /** The arguments used to lookup exception requests: (Exception) */
  type ExceptionArgs = String

  /** The key used to lookup exception requests */
  type ExceptionKey = String

  private val exceptionArgsToRequestId =
    new ConcurrentHashMap[ExceptionArgs, ExceptionKey]().asScala

  private val exceptionRequests =
    new ConcurrentHashMap[ExceptionKey, Seq[ExceptionRequest]]().asScala

  @volatile private var catchallExceptionRequest: Option[
    (ExceptionKey, ExceptionRequest)
  ] = None

  /**
   * Retrieves the list of exception requests contained by this manager.
   *
   * @return The collection of exception requests by full exception class name
   */
  def exceptionRequestList: Seq[ExceptionArgs] =
    exceptionArgsToRequestId.keySet.toSeq

  /**
   * Retrieves the list of exception requests contained by this manager.
   *
   * @return The collection of exception requests by id
   */
  def exceptionRequestListById: Seq[ExceptionKey] =
    exceptionRequests.keySet.toSeq

  /**
   * Creates a new exception request to catch all exceptions from the JVM.
   *
   * @note The request id given does not get added to the request id list and
   *       removing by id will not remove this request instance.
   *
   * @param requestId The id associated with the requests for lookup and removal
   * @param notifyCaught If true, events will be reported when any exception
   *                     is detected within a try { ... } block
   * @param notifyUncaught If true, events will be reported when any exception
   *                       is detected not within a try { ... } block
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createCatchallExceptionRequestWithId(
    requestId: String,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIRequestArgument*
  ): Try[ExceptionKey] = {
    val arguments = Seq(
      EnabledProperty(value = true),
      SuspendPolicyProperty.EventThread
    ) ++ extraArguments

    val request = Try(eventRequestManager.createExceptionRequest(
      null, notifyCaught, notifyUncaught, arguments: _*
    ))

    if (request.isSuccess) {
      catchallExceptionRequest = Some((requestId, request.get))
      exceptionRequests.put(requestId, Seq(request.get))
    }

    // If no exception was thrown, assume that we succeeded
    request.map(_ => requestId)
  }

  /**
   * Creates a new exception request to catch all exceptions from the JVM.
   *
   * @note The request id given does not get added to the request id list and
   *       removing by id will not remove this request instance.
   *
   * @param notifyCaught If true, events will be reported when any exception
   *                     is detected within a try { ... } block
   * @param notifyUncaught If true, events will be reported when any exception
   *                       is detected not within a try { ... } block
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createCatchallExceptionRequest(
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIRequestArgument*
  ): Try[ExceptionKey] = {
    createCatchallExceptionRequestWithId(
      newRequestId(),
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    )
  }

  /**
   * Retrieves the id of the exception request used to catch all exceptions.
   *
   * @return Some id if the catchall has been set, otherwise None
   */
  def getCatchallExceptionRequestId: Option[ExceptionKey] = {
    catchallExceptionRequest.map(_._1)
  }

  /**
   * Determines if the exception request to catch all exceptions has been set.
   *
   * @return True if set, otherwise false
   */
  def hasCatchallExceptionRequest: Boolean = catchallExceptionRequest.nonEmpty

  /**
   * Retrieves the exception request used to catch all exceptions.
   *
   * @return Some exception request if the catchall has been set, otherwise None
   */
  def getCatchallExceptionRequest: Option[ExceptionRequest] =
    catchallExceptionRequest.map(_._2)

  /**
   * Removes the exception request used to catch all exceptions.
   *
   * @return True if the exception request was removed (if it existed),
   *         otherwise false
   */
  def removeCatchallExceptionRequest(): Boolean =
    catchallExceptionRequest.synchronized {
      catchallExceptionRequest match {
        case Some(r) =>
          eventRequestManager.deleteEventRequest(r._2)
          catchallExceptionRequest = None
          true
        case None =>
          false
      }
    }

  /**
   * Creates a new exception request for the specified exception class.
   *
   * @note Any exception and its subclass will be watched.
   *
   * @param requestId The id associated with the requests for lookup and removal
   * @param exceptionName The full class name of the exception to watch
   * @param notifyCaught If true, events will be reported when the exception
   *                     is detected within a try { ... } block
   * @param notifyUncaught If true, events will be reported when the exception
   *                       is detected not within a try { ... } block
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createExceptionRequestWithId(
    requestId: String,
    exceptionName: String,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIRequestArgument*
  ): Try[ExceptionKey] = {
    val exceptionReferenceTypes = virtualMachine.classesByName(exceptionName)

    // If no classes match the requested exception type, exit early
    if (exceptionReferenceTypes.isEmpty)
      return Failure(NoExceptionClassFound(exceptionName))

    val arguments = Seq(
      EnabledProperty(value = true),
      SuspendPolicyProperty.EventThread
    ) ++ extraArguments

    // TODO: Back out exception creation if a failure occurs
    val requests = Try(exceptionReferenceTypes.asScala.map(
      eventRequestManager.createExceptionRequest(
        _: ReferenceType, notifyCaught, notifyUncaught, arguments: _*
      )
    ))

    if (requests.isSuccess) {
      exceptionArgsToRequestId.put(exceptionName, requestId)
      exceptionRequests.put(requestId, requests.get)
    }

    // If no exception was thrown, assume that we succeeded
    requests.map(_ => requestId)
  }

  /**
   * Creates a new exception request for the specified exception class.
   *
   * @note Any exception and its subclass will be watched.
   *
   * @param exceptionName The full class name of the exception to watch
   * @param notifyCaught If true, events will be reported when the exception
   *                     is detected within a try { ... } block
   * @param notifyUncaught If true, events will be reported when the exception
   *                       is detected not within a try { ... } block
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createExceptionRequest(
    exceptionName: String,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIRequestArgument*
  ): Try[ExceptionKey] = {
    createExceptionRequestWithId(
      newRequestId(),
      exceptionName,
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    )
  }

  /**
   * Determines if an exception request exists for the specified exception
   * class name.
   *
   * @param exceptionName The full class name of the exception targeted by the
   *                      exception requests
   *
   * @return True if a exception request exists, otherwise false
   */
  def hasExceptionRequest(exceptionName: String): Boolean = {
    exceptionArgsToRequestId.get(exceptionName)
      .exists(hasExceptionRequestWithId)
  }

  /**
   * Determines if an exception request exists with the specified id.
   *
   * @param requestId The id of the request used to retrieve and delete it
   *
   * @return True if a exception request exists, otherwise false
   */
  def hasExceptionRequestWithId(requestId: String): Boolean = {
    exceptionRequests.contains(requestId)
  }

  /**
   * Retrieves the collection of exception requests with the matching exception
   * class name.
   *
   * @param exceptionName The full class name of the exception targeted by the
   *                      exception requests
   *
   * @return Some collection of exception requests if they exist, otherwise None
   */
  def getExceptionRequest(
    exceptionName: String
  ): Option[Seq[ExceptionRequest]] = {
    exceptionArgsToRequestId.get(exceptionName)
      .flatMap(getExceptionRequestWithId)
  }

  /**
   * Retrieves the collection of exception requests with the specified id.
   *
   * @param requestId The id of the request used to retrieve and delete it
   *
   * @return Some collection of exception requests if they exist, otherwise None
   */
  def getExceptionRequestWithId(
    requestId: String
  ): Option[Seq[ExceptionRequest]] = {
    exceptionRequests.get(requestId)
  }

  /**
   * Removes the specified exception requests with the matching exception
   * class name.
   *
   * @param exceptionName The full class name of the exception targeted by the
   *                      exception requests
   *
   * @return True if the exception requests were removed (if they existed),
   *         otherwise false
   */
  def removeExceptionRequest(exceptionName: String): Boolean = {
    exceptionArgsToRequestId.get(exceptionName)
      .exists(removeExceptionRequestWithId)
  }

  /**
   * Removes the exception request with the specified id.
   *
   * @param requestId The id of the request
   *
   * @return True if the exception request was removed (if it existed),
   *         otherwise false
   */
  def removeExceptionRequestWithId(requestId: String): Boolean = {
    val requests = exceptionRequests.remove(requestId)

    // Reverse-lookup arguments to remove argsToId mapping
    exceptionArgsToRequestId.find(_._2 == requestId).map(_._1)
      .foreach(exceptionArgsToRequestId.remove)

    requests.map(_.asJava).foreach(eventRequestManager.deleteEventRequests)

    requests.nonEmpty
  }

  /**
   * Generates an id for a new request.
   *
   * @return The id as a string
   */
  protected def newRequestId(): String = java.util.UUID.randomUUID().toString
}
