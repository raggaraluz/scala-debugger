package org.scaladebugger.api.profiles.traits.classes
import acyclic.file

import com.sun.jdi.event.ClassPrepareEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.classes.ClassPrepareRequestInfo
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline

import scala.util.Try

/**
 * Represents the interface that needs to be implemented to provide
 * class prepare functionality for a specific debug profile.
 */
trait ClassPrepareProfile {
  /** Represents a class prepare event and any associated data. */
  type ClassPrepareEventAndData = (ClassPrepareEvent, Seq[JDIEventDataResult])

  /**
   * Retrieves the collection of active and pending class prepare requests.
   *
   * @return The collection of information on class prepare requests
   */
  def classPrepareRequests: Seq[ClassPrepareRequestInfo]

  /**
   * Constructs a stream of class prepare events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of class prepare events
   */
  def tryGetOrCreateClassPrepareRequest(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ClassPrepareEvent]] = {
    tryGetOrCreateClassPrepareRequestWithData(extraArguments: _*).map(_.map(_._1).noop())
  }

  /**
   * Constructs a stream of class prepare events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of class prepare events
   */
  def getOrCreateClassPrepareRequest(
    extraArguments: JDIArgument*
  ): IdentityPipeline[ClassPrepareEvent] = {
    tryGetOrCreateClassPrepareRequest(extraArguments: _*).get
  }

  /**
   * Constructs a stream of class prepare events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of class prepare events and any retrieved data based on
   *         requests from extra arguments
   */
  def getOrCreateClassPrepareRequestWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[ClassPrepareEventAndData] = {
    tryGetOrCreateClassPrepareRequestWithData(extraArguments: _*).get
  }

  /**
   * Constructs a stream of class prepare events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of class prepare events and any retrieved data based on
   *         requests from extra arguments
   */
  def tryGetOrCreateClassPrepareRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ClassPrepareEventAndData]]
}
