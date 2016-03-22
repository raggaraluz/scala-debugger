package org.scaladebugger.api.dsl.exceptions

import com.sun.jdi.event.ExceptionEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.exceptions.ExceptionProfile

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param exceptionProfile The profile to wrap
 */
class ExceptionDSLWrapper private[dsl] (
  private val exceptionProfile: ExceptionProfile
) {
  /** Represents a Exception event and any associated data. */
  type ExceptionEventAndData = (ExceptionEvent, Seq[JDIEventDataResult])

  /** @see ExceptionProfile#tryGetOrCreateExceptionRequest(String, Boolean, Boolean, JDIArgument*) */
  def onException(
    exceptionName: String,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ExceptionEvent]] =
    exceptionProfile.tryGetOrCreateExceptionRequest(
      exceptionName,
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    )

  /** @see ExceptionProfile#getOrCreateExceptionRequest(String, Boolean, Boolean, JDIArgument*) */
  def onUnsafeException(
    exceptionName: String,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): IdentityPipeline[ExceptionEvent] =
    exceptionProfile.getOrCreateExceptionRequest(
      exceptionName,
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    )

  /** @see ExceptionProfile#getOrCreateExceptionRequestWithData(String, Boolean, Boolean, JDIArgument*) */
  def onUnsafeExceptionWithData(
    exceptionName: String,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): IdentityPipeline[ExceptionEventAndData] =
    exceptionProfile.getOrCreateExceptionRequestWithData(
      exceptionName,
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    )

  /** @see ExceptionProfile#tryGetOrCreateExceptionRequestWithData(String, Boolean, Boolean, JDIArgument*) */
  def onExceptionWithData(
    exceptionName: String,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ExceptionEventAndData]] =
    exceptionProfile.tryGetOrCreateExceptionRequestWithData(
      exceptionName,
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    )

  /** @see ExceptionProfile#tryGetOrCreateAllExceptionsRequest(Boolean, Boolean, JDIArgument*) */
  def onAllExceptions(
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ExceptionEvent]] =
    exceptionProfile.tryGetOrCreateAllExceptionsRequest(
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    )

  /** @see ExceptionProfile#getOrCreateAllExceptionsRequest(Boolean, Boolean, JDIArgument*) */
  def onUnsafeAllExceptions(
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): IdentityPipeline[ExceptionEvent] =
    exceptionProfile.getOrCreateAllExceptionsRequest(
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    )

  /** @see ExceptionProfile#getOrCreateAllExceptionsRequestWithData(Boolean, Boolean, JDIArgument*) */
  def onUnsafeAllExceptionsWithData(
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): IdentityPipeline[ExceptionEventAndData] =
    exceptionProfile.getOrCreateAllExceptionsRequestWithData(
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    )

  /** @see ExceptionProfile#tryGetOrCreateAllExceptionsRequestWithData(Boolean, Boolean, JDIArgument*) */
  def onAllExceptionsWithData(
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ExceptionEventAndData]] =
    exceptionProfile.tryGetOrCreateAllExceptionsRequestWithData(
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    )
}
