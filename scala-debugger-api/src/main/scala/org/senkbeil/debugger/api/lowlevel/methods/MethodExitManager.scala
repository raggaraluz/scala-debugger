package org.senkbeil.debugger.api.lowlevel.methods

import java.util.concurrent.ConcurrentHashMap

import com.sun.jdi.VirtualMachine
import com.sun.jdi.request.{EventRequestManager, MethodExitRequest}
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.filters.ClassInclusionFilter
import org.senkbeil.debugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.senkbeil.debugger.api.lowlevel.requests.Implicits._
import org.senkbeil.debugger.api.lowlevel.utils.JDIHelperMethods
import org.senkbeil.debugger.api.utils.Logging

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

/**
 * Represents the manager for method exit requests.
 *
 * @param eventRequestManager The manager used to create method exit requests
 */
class MethodExitManager(
  private val eventRequestManager: EventRequestManager
) extends Logging {
  type MethodExitKey = (String, String)
  private val methodExitRequests =
    new ConcurrentHashMap[MethodExitKey, MethodExitRequest]()

  /**
   * Retrieves the list of method exit requests contained by this manager.
   *
   * @return The collection of method exit requests in the form of
   *         (class name, method name)
   */
  def methodExitRequestList: Seq[MethodExitKey] =
    methodExitRequests.keySet().asScala.toSeq

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
   * @return True if successful in creating the request, otherwise false
   */
  def createMethodExitRequest(
    className: String,
    methodName: String,
    extraArguments: JDIRequestArgument*
  ): Try[Boolean] = {
    val request = Try(eventRequestManager.createMethodExitRequest(
      Seq(
        ClassInclusionFilter(classPattern = className),
        EnabledProperty(value = true),
        SuspendPolicyProperty.EventThread
      ) ++ extraArguments: _*
    ))

    if (request.isSuccess) {
      methodExitRequests.put((className, methodName), request.get)
    }

    // If no exception was thrown, assume that we succeeded
    request.map(_ => true)
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
    methodExitRequests.containsKey((className, methodName))
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
    Option(methodExitRequests.get((className, methodName)))
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
    val request = Option(methodExitRequests.remove((className, methodName)))

    request.foreach(eventRequestManager.deleteEventRequest)

    request.nonEmpty
  }
}
