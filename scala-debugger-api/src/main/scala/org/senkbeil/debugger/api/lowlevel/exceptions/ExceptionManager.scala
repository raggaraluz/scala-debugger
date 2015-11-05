package org.senkbeil.debugger.api.lowlevel.exceptions

import java.util.concurrent.ConcurrentHashMap

import com.sun.jdi.{ReferenceType, VirtualMachine}
import com.sun.jdi.request.{EventRequestManager, ExceptionRequest}
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.{SuspendPolicyProperty, EnabledProperty}
import org.senkbeil.debugger.api.lowlevel.utils.JDIHelperMethods
import org.senkbeil.debugger.api.utils.Logging
import scala.collection.JavaConverters._
import org.senkbeil.debugger.api.lowlevel.requests.Implicits._

import scala.util.{Failure, Success, Try}

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
  type ExceptionKey = String
  private val exceptionRequests =
    new ConcurrentHashMap[ExceptionKey, Seq[ExceptionRequest]]()

  @volatile
  private var catchallExceptionRequest: Option[ExceptionRequest] = None

  /**
   * Retrieves the list of exception requests contained by this manager.
   *
   * @return The collection of exception requests by full exception class name
   */
  def exceptionRequestList: Seq[ExceptionKey] =
    exceptionRequests.keySet().asScala.toSeq

  /**
   * Sets the exception request to catch all exceptions from the JVM.
   *
   * @param notifyCaught If true, events will be reported when any exception
   *                     is detected within a try { ... } block
   * @param notifyUncaught If true, events will be reported when any exception
   *                       is detected not within a try { ... } block
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return True if successful in creating the request, otherwise false
   */
  def createCatchallExceptionRequest(
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIRequestArgument*
  ): Try[Boolean] = {
    val arguments = Seq(
      EnabledProperty(value = true),
      SuspendPolicyProperty.EventThread
    ) ++ extraArguments

    val request = Try(eventRequestManager.createExceptionRequest(
      null, notifyCaught, notifyUncaught, arguments: _*
    ))

    if (request.isSuccess) {
      catchallExceptionRequest = Some(request.get)
    }

    // If no exception was thrown, assume that we succeeded
    request.map(_ => true)
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
    catchallExceptionRequest

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
          eventRequestManager.deleteEventRequest(r)
          catchallExceptionRequest = None
          true
        case None =>
          false
      }
    }

  /**
   * Sets the exception request for the specified exception class.
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
   * @return True if successful in creating the request, otherwise false
   */
  def createExceptionRequest(
    exceptionName: String,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIRequestArgument*
  ): Try[Boolean] = {
    val exceptionReferenceTypes = virtualMachine.classesByName(exceptionName)

    // If no classes match the requested exception type, exit early
    if (exceptionReferenceTypes.isEmpty) return Success(false)

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
      exceptionRequests.put(exceptionName, requests.get)
    }

    // If no exception was thrown, assume that we succeeded
    requests.map(_ => true)
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
    exceptionRequests.containsKey(exceptionName)
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
    Option(exceptionRequests.get(exceptionName))
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
    val requests = Option(exceptionRequests.remove(exceptionName))

    requests.foreach(_.foreach(eventRequestManager.deleteEventRequest))

    requests.nonEmpty
  }
}
