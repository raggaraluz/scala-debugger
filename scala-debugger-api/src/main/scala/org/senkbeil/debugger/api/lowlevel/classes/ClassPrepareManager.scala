package org.senkbeil.debugger.api.lowlevel.classes

import java.util.concurrent.ConcurrentHashMap

import com.sun.jdi.request.{ClassPrepareRequest, EventRequestManager}
import org.senkbeil.debugger.api.lowlevel.requests.Implicits._
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.senkbeil.debugger.api.utils.{MultiMap, Logging}

import scala.collection.JavaConverters._
import scala.util.Try

/**
 * Represents the manager for class prepare requests.
 *
 * @param eventRequestManager The manager used to create class prepare requests
 */
class ClassPrepareManager(
  private val eventRequestManager: EventRequestManager
) extends Logging {
  private val classPrepareRequests =
    new MultiMap[Seq[JDIRequestArgument], ClassPrepareRequest]

  /**
   * Retrieves the list of class prepare requests contained by this manager.
   *
   * @return The collection of class prepare requests in the form of ids
   */
  def classPrepareRequestList: Seq[String] = classPrepareRequests.ids

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
  ): Try[String] = {
    val request = Try(eventRequestManager.createClassPrepareRequest(
      Seq(
        EnabledProperty(value = true),
        SuspendPolicyProperty.EventThread
      ) ++ extraArguments: _*
    ))

    if (request.isSuccess) {
      classPrepareRequests.putWithId(requestId, extraArguments, request.get)
    }

    // If no exception was thrown, assume that we succeeded
    request.map(_ => requestId)
  }

  /**
   * Creates a new class prepare request.
   *
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createClassPrepareRequest(
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    createClassPrepareRequestWithId(newRequestId(), extraArguments: _*)
  }

  /**
   * Determines if a class prepare request with the specified id.
   *
   * @param id The id of the Class Prepare Request
   *
   * @return True if a class prepare request with the id exists, otherwise false
   */
  def hasClassPrepareRequest(id: String): Boolean = {
    classPrepareRequests.hasWithId(id)
  }

  /**
   * Retrieves the class prepare request using the specified id.
   *
   * @param id The id of the Class Prepare Request
   *
   * @return Some class prepare request if it exists, otherwise None
   */
  def getClassPrepareRequest(id: String): Option[ClassPrepareRequest] = {
    classPrepareRequests.getWithId(id)
  }

  /**
   * Retrieves the arguments provided to the class prepare request with the
   * specified id.
   *
   * @param id The id of the Thread Start Request
   *
   * @return Some collection of arguments if it exists, otherwise None
   */
  def getClassPrepareRequestArguments(
    id: String
  ): Option[Seq[JDIRequestArgument]] = {
    classPrepareRequests.getKeyWithId(id)
  }

  /**
   * Removes the specified class prepare request.
   *
   * @param id The id of the Class Prepare Request
   *
   * @return True if the class prepare request was removed (if it existed),
   *         otherwise false
   */
  def removeClassPrepareRequest(id: String): Boolean = {
    val request = classPrepareRequests.removeWithId(id)

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
