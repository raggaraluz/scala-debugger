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

  override def stepInLineWithData(
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = {
    withCurrentProfile.stepInLineWithData(extraArguments: _*)
  }

  override def stepOverLineWithData(
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = {
    withCurrentProfile.stepOverLineWithData(extraArguments: _*)
  }

  override def stepOutLineWithData(
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = {
    withCurrentProfile.stepOutLineWithData(extraArguments: _*)
  }

  override def stepInMinWithData(
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = {
    withCurrentProfile.stepInMinWithData(extraArguments: _*)
  }

  override def stepOverMinWithData(
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = {
    withCurrentProfile.stepOverMinWithData(extraArguments: _*)
  }

  override def stepOutMinWithData(
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = {
    withCurrentProfile.stepOutMinWithData(extraArguments: _*)
  }
}
