package org.senkbeil.debugger.api.lowlevel.classes

import java.util.concurrent.ConcurrentHashMap

import com.sun.jdi.request.{EventRequestManager, ClassUnloadRequest}
import org.senkbeil.debugger.api.lowlevel.requests.Implicits._
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.senkbeil.debugger.api.utils.Logging

import scala.collection.JavaConverters._
import scala.util.Try

/**
 * Represents the manager for class unload requests.
 *
 * @param eventRequestManager The manager used to create class unload requests
 */
class ClassUnloadManager(
  private val eventRequestManager: EventRequestManager
) extends Logging {
  type ClassUnloadKey = String
  private val classUnloadRequests = new ConcurrentHashMap[
    ClassUnloadKey,
    (Seq[JDIRequestArgument], ClassUnloadRequest)
  ]()

  /**
   * Retrieves the list of class unload requests contained by this manager.
   *
   * @return The collection of class unload requests in the form of
   *         (class name, method name)
   */
  def classUnloadRequestList: Seq[ClassUnloadKey] =
    classUnloadRequests.keySet().asScala.toSeq

  /**
   * Creates a new class unload request.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createClassUnloadRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[ClassUnloadKey] = {
    val request = Try(eventRequestManager.createClassUnloadRequest(
      Seq(
        EnabledProperty(value = true),
        SuspendPolicyProperty.EventThread
      ) ++ extraArguments: _*
    ))

    if (request.isSuccess) {
      classUnloadRequests.put(requestId, (extraArguments, request.get))
    }

    // If no exception was thrown, assume that we succeeded
    request.map(_ => requestId)
  }

  /**
   * Creates a new class unload request.
   *
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createClassUnloadRequest(
    extraArguments: JDIRequestArgument*
  ): Try[ClassUnloadKey] = {
    createClassUnloadRequestWithId(newRequestId(), extraArguments: _*)
  }

  /**
   * Determines if a class unload request with the specified id.
   *
   * @param id The id of the Class Unload Request
   *
   * @return True if a class unload request with the id exists, otherwise false
   */
  def hasClassUnloadRequest(id: ClassUnloadKey): Boolean = {
    classUnloadRequests.containsKey(id)
  }

  /**
   * Retrieves the class unload request using the specified id.
   *
   * @param id The id of the Class Unload Request
   *
   * @return Some class unload request if it exists, otherwise None
   */
  def getClassUnloadRequest(id: ClassUnloadKey): Option[ClassUnloadRequest] = {
    Option(classUnloadRequests.get(id)).map(_._2)
  }

  /**
   * Retrieves the arguments provided to the class unload request with the
   * specified id.
   *
   * @param id The id of the Thread Start Request
   *
   * @return Some collection of arguments if it exists, otherwise None
   */
  def getClassUnloadRequestArguments(
    id: ClassUnloadKey
  ): Option[Seq[JDIRequestArgument]] = {
    Option(classUnloadRequests.get(id)).map(_._1)
  }

  /**
   * Removes the specified class unload request.
   *
   * @param id The id of the Class Unload Request
   *
   * @return True if the class unload request was removed (if it existed),
   *         otherwise false
   */
  def removeClassUnloadRequest(id: ClassUnloadKey): Boolean = {
    val request = Option(classUnloadRequests.remove(id)).map(_._2)

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
