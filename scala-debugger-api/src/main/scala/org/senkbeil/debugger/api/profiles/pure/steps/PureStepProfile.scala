package org.senkbeil.debugger.api.profiles.pure.steps

import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.profiles.traits.steps.StepProfile

import scala.concurrent.Future

/**
 * Represents a pure profile for steps that adds no
 * extra logic on top of the standard JDI.
 */
trait PureStepProfile extends StepProfile {
  /**
   * Steps in from the current location to the next line.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The resulting event and any retrieved data based on
   *         requests from extra arguments
   */
  override def stepInLineWithData(
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = ???

  /**
   * Steps over from the current location to the next line.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The resulting event and any retrieved data based on
   *         requests from extra arguments
   */
  override def stepOverLineWithData(
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = ???

  /**
   * Constructs a stream of step events caused by stepping out from the
   * current location to the next line.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The resulting event and any retrieved data based on
   *         requests from extra arguments
   */
  override def stepOutLineWithData(
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = ???

  /**
   * Steps in from the current location to the next location.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The resulting event and any retrieved data based on
   *         requests from extra arguments
   */
  override def stepInMinWithData(
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = ???

  /**
   * Steps over from the current location to the next location.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The resulting event and any retrieved data based on
   *         requests from extra arguments
   */
  override def stepOverMinWithData(
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = ???

  /**
   * Constructs a stream of step events caused by stepping out from the
   * current location to the next location.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The resulting event and any retrieved data based on
   *         requests from extra arguments
   */
  override def stepOutMinWithData(
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = ???
}
