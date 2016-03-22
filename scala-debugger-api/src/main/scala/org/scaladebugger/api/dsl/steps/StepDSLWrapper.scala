package org.scaladebugger.api.dsl.steps

import com.sun.jdi.ThreadReference
import com.sun.jdi.event.StepEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.steps.StepProfile

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param stepProfile The profile to wrap
 */
class StepDSLWrapper private[dsl] (
  private val stepProfile: StepProfile
) {
  /** Represents a Step event and any associated data. */
  type StepStepAndData = (StepEvent, Seq[JDIEventDataResult])

  /** @see StepProfile#tryCreateStepListener(ThreadReference, JDIArgument*) */
  def onStep(
    threadReference: ThreadReference,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[StepEvent]] =
    stepProfile.tryCreateStepListener(threadReference, extraArguments: _*)

  /** @see StepProfile#createStepListener(ThreadReference, JDIArgument*) */
  def onUnsafeStep(
    threadReference: ThreadReference,
    extraArguments: JDIArgument*
  ): IdentityPipeline[StepEvent] =
    stepProfile.createStepListener(threadReference, extraArguments: _*)

  /** @see StepProfile#createStepListenerWithData(ThreadReference, JDIArgument*) */
  def onUnsafeStepWithData(
    threadReference: ThreadReference,
    extraArguments: JDIArgument*
  ): IdentityPipeline[StepStepAndData] =
    stepProfile.createStepListenerWithData(threadReference, extraArguments: _*)

  /** @see StepProfile#tryCreateStepListenerWithData(ThreadReference, JDIArgument*) */
  def onStepWithData(
    threadReference: ThreadReference,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[StepStepAndData]] =
    stepProfile.tryCreateStepListenerWithData(threadReference, extraArguments: _*)
}
