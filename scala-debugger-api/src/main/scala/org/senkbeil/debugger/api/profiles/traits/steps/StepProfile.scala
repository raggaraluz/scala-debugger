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
   * Steps in from the current location.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The resulting event
   */
  def stepIn(extraArguments: JDIArgument*): Future[StepEvent] = {
    stepInWithData(extraArguments: _*).map(_._1)
  }

  /**
   * Steps in from the current location.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The resulting event and any retrieved data based on
   *         requests from extra arguments
   */
  def stepInWithData(extraArguments: JDIArgument*): Future[StepEventAndData]

  /**
   * Steps over from the current location.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The resulting event
   */
  def stepOver(extraArguments: JDIArgument*): Future[StepEvent] = {
    stepOverWithData(extraArguments: _*).map(_._1)
  }

  /**
   * Steps over from the current location.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The resulting event and any retrieved data based on
   *         requests from extra arguments
   */
  def stepOverWithData(extraArguments: JDIArgument*): Future[StepEventAndData]

  /**
   * Constructs a stream of step events caused by stepping out from the
   * current location.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The resulting event
   */
  def stepOut(extraArguments: JDIArgument*): Future[StepEvent] = {
    stepOutWithData(extraArguments: _*).map(_._1)
  }

  /**
   * Constructs a stream of step events caused by stepping out from the
   * current location.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The resulting event and any retrieved data based on
   *         requests from extra arguments
   */
  def stepOutWithData(extraArguments: JDIArgument*): Future[StepEventAndData]
}
