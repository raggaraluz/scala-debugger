package org.senkbeil.debugger.api.profiles.swappable.steps

import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.profiles.swappable.SwappableDebugProfile
import org.senkbeil.debugger.api.profiles.traits.steps.StepProfile

import scala.concurrent.Future

/**
 * Represents a swappable profile for step events that redirects the
 * invocation to another profile.
 */
trait SwappableStepProfile extends StepProfile {
  this: SwappableDebugProfile =>

  override def stepInWithData(
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = {
    withCurrentProfile.stepInWithData(extraArguments: _*)
  }

  override def stepOverWithData(
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = {
    withCurrentProfile.stepOverWithData(extraArguments: _*)
  }

  override def stepOutWithData(
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = {
    withCurrentProfile.stepOutWithData(extraArguments: _*)
  }
}
