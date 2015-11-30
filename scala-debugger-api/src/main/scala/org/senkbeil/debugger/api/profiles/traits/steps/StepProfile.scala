package org.senkbeil.debugger.api.profiles.traits.steps

import com.sun.jdi.event.StepEvent
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global
/**
 * Represents the interface that needs to be implemented to provide
 * step functionality for a specific debug profile.
 */
trait StepProfile {
  /** Represents a step event and any associated data. */
  type StepEventAndData = (StepEvent, Seq[JDIEventDataResult])

  /**
   * Steps in from the current location to the next line.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The resulting event
   */
  def stepInLine(extraArguments: JDIArgument*): Future[StepEvent] = {
    stepInLineWithData(extraArguments: _*).map(_._1)
  }

  /**
   * Steps in from the current location to the next line.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The resulting event and any retrieved data based on
   *         requests from extra arguments
   */
  def stepInLineWithData(extraArguments: JDIArgument*): Future[StepEventAndData]

  /**
   * Steps over from the current location to the next line.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The resulting event
   */
  def stepOverLine(extraArguments: JDIArgument*): Future[StepEvent] = {
    stepOverLineWithData(extraArguments: _*).map(_._1)
  }

  /**
   * Steps over from the current location to the next line.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The resulting event and any retrieved data based on
   *         requests from extra arguments
   */
  def stepOverLineWithData(
    extraArguments: JDIArgument*
  ): Future[StepEventAndData]

  /**
   * Constructs a stream of step events caused by stepping out from the
   * current location to the next line.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The resulting event
   */
  def stepOutLine(extraArguments: JDIArgument*): Future[StepEvent] = {
    stepOutLineWithData(extraArguments: _*).map(_._1)
  }

  /**
   * Constructs a stream of step events caused by stepping out from the
   * current location to the next line.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The resulting event and any retrieved data based on
   *         requests from extra arguments
   */
  def stepOutLineWithData(
    extraArguments: JDIArgument*
  ): Future[StepEventAndData]

  /**
   * Steps in from the current location to the next location.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The resulting event
   */
  def stepInMin(extraArguments: JDIArgument*): Future[StepEvent] = {
    stepInMinWithData(extraArguments: _*).map(_._1)
  }

  /**
   * Steps in from the current location to the next location.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The resulting event and any retrieved data based on
   *         requests from extra arguments
   */
  def stepInMinWithData(extraArguments: JDIArgument*): Future[StepEventAndData]

  /**
   * Steps over from the current location to the next location.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The resulting event
   */
  def stepOverMin(extraArguments: JDIArgument*): Future[StepEvent] = {
    stepOverMinWithData(extraArguments: _*).map(_._1)
  }

  /**
   * Steps over from the current location to the next location.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The resulting event and any retrieved data based on
   *         requests from extra arguments
   */
  def stepOverMinWithData(
    extraArguments: JDIArgument*
  ): Future[StepEventAndData]

  /**
   * Constructs a stream of step events caused by stepping out from the
   * current location to the next location.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The resulting event
   */
  def stepOutMin(extraArguments: JDIArgument*): Future[StepEvent] = {
    stepOutMinWithData(extraArguments: _*).map(_._1)
  }

  /**
   * Constructs a stream of step events caused by stepping out from the
   * current location to the next location.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The resulting event and any retrieved data based on
   *         requests from extra arguments
   */
  def stepOutMinWithData(
    extraArguments: JDIArgument*
  ): Future[StepEventAndData]
}
