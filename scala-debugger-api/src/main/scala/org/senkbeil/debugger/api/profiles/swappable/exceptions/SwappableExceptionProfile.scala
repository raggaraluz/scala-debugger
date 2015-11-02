package org.senkbeil.debugger.api.profiles.swappable.exceptions

import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.swappable.SwappableDebugProfile
import org.senkbeil.debugger.api.profiles.traits.exceptions.ExceptionProfile

/**
 * Represents a swappable profile for exception events that redirects the
 * invocation to another profile.
 */
trait SwappableExceptionProfile extends ExceptionProfile {
  this: SwappableDebugProfile =>

  override def onExceptionWithData(
    exceptionName: String,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): IdentityPipeline[ExceptionEventAndData] = {
    withCurrentProfile.onExceptionWithData(
      exceptionName,
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    )
  }

  override def onAllExceptionsWithData(
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): IdentityPipeline[ExceptionEventAndData] = {
    withCurrentProfile.onAllExceptionsWithData(
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    )
  }
}
