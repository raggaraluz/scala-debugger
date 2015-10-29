package org.senkbeil.debugger.api.profiles.pure.steps

import com.sun.jdi.event.StepEvent
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.profiles.traits.steps.StepProfile

import scala.concurrent.Future

/**
 * Represents a pure profile for steps that adds no
 * extra logic on top of the standard JDI.
 */
trait PureStepProfile extends StepProfile {
  /**
   * Steps in from the current location.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The resulting event and any retrieved data based on
   *         requests from extra arguments
   */
  override def stepInWithData(
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = ???

  /**
   * Steps over from the current location.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The resulting event and any retrieved data based on
   *         requests from extra arguments
   */
  override def stepOverWithData(
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = ???

  /**
   * Constructs a stream of step events caused by stepping out from the
   * current location.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The resulting event and any retrieved data based on
   *         requests from extra arguments
   */
  override def stepOutWithData(
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = ???
}
