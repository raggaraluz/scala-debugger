package org.senkbeil.debugger.api.profiles.swappable.steps

import com.sun.jdi.ThreadReference
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
    threadReference: ThreadReference,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = {
    withCurrentProfile.stepInLineWithData(threadReference, extraArguments: _*)
  }

  override def stepOverLineWithData(
    threadReference: ThreadReference,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = {
    withCurrentProfile.stepOverLineWithData(threadReference, extraArguments: _*)
  }

  override def stepOutLineWithData(
    threadReference: ThreadReference,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = {
    withCurrentProfile.stepOutLineWithData(threadReference, extraArguments: _*)
  }

  override def stepInMinWithData(
    threadReference: ThreadReference,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = {
    withCurrentProfile.stepInMinWithData(threadReference, extraArguments: _*)
  }

  override def stepOverMinWithData(
    threadReference: ThreadReference,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = {
    withCurrentProfile.stepOverMinWithData(threadReference, extraArguments: _*)
  }

  override def stepOutMinWithData(
    threadReference: ThreadReference,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = {
    withCurrentProfile.stepOutMinWithData(threadReference, extraArguments: _*)
  }
}
