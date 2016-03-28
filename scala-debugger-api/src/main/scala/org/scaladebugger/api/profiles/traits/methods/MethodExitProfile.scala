package org.scaladebugger.api.profiles.traits.methods
import acyclic.file

import com.sun.jdi.event.MethodExitEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.methods.MethodExitRequestInfo
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline

import scala.util.Try

/**
 * Represents the interface that needs to be implemented to provide
 * method exit functionality for a specific debug profile.
 */
trait MethodExitProfile {
  /** Represents a method exit event and any associated data. */
  type MethodExitEventAndData = (MethodExitEvent, Seq[JDIEventDataResult])

  /**
   * Retrieves the collection of active and pending method exit requests.
   *
   * @return The collection of information on method exit requests
   */
  def methodExitRequests: Seq[MethodExitRequestInfo]

  /**
   * Constructs a stream of method exit events for the specified class and
   * method.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method to watch
   * @param methodName The name of the method to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of method exit events
   */
  def tryGetOrCreateMethodExitRequest(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MethodExitEvent]] = {
    tryGetOrCreateMethodExitRequestWithData(
      className: String,
      methodName: String,
      extraArguments: _*
    ).map(_.map(_._1).noop())
  }

  /**
   * Constructs a stream of method exit events for the specified class and
   * method.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method to watch
   * @param methodName The name of the method to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of method exit events
   */
  def getOrCreateMethodExitRequest(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): IdentityPipeline[MethodExitEvent] = {
    tryGetOrCreateMethodExitRequest(
      className,
      methodName,
      extraArguments: _*
    ).get
  }

  /**
   * Constructs a stream of method exit events for the specified class and
   * method.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method to watch
   * @param methodName The name of the method to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of method exit events and any retrieved data based on
   *         requests from extra arguments
   */
  def getOrCreateMethodExitRequestWithData(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): IdentityPipeline[MethodExitEventAndData] = {
    tryGetOrCreateMethodExitRequestWithData(
      className,
      methodName,
      extraArguments: _*
    ).get
  }

  /**
   * Constructs a stream of method exit events for the specified class and
   * method.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method to watch
   * @param methodName The name of the method to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of method exit events and any retrieved data based on
   *         requests from extra arguments
   */
  def tryGetOrCreateMethodExitRequestWithData(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MethodExitEventAndData]]

  /**
   * Determines if there is any method exit request for the specified class
   * method that is pending.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method being watched
   * @param methodName The name of the method being watched
   * @return True if there is at least one method exit request with the
   *         specified name in the specified class that is pending,
   *         otherwise false
   */
  def isMethodExitRequestPending(
    className: String,
    methodName: String
  ): Boolean

  /**
   * Determines if there is any method exit request for the specified class
   * method with matching arguments that is pending.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method being watched
   * @param methodName The name of the method being watched
   * @param extraArguments The additional arguments provided to the specific
   *                       method exit request
   * @return True if there is at least one method exit request with the
   *         specified name and arguments in the specified class that is
   *         pending, otherwise false
   */
  def isMethodExitRequestWithArgsPending(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Boolean
}
