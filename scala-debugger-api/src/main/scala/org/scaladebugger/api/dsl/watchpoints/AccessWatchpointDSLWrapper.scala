package org.scaladebugger.api.dsl.watchpoints

import com.sun.jdi.event.AccessWatchpointEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.watchpoints.AccessWatchpointProfile

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param accessWatchpointProfile The profile to wrap
 */
class AccessWatchpointDSLWrapper private[dsl] (
  private val accessWatchpointProfile: AccessWatchpointProfile
) {
  /** Represents a AccessWatchpoint event and any associated data. */
  type AccessWatchpointEventAndData = (AccessWatchpointEvent, Seq[JDIEventDataResult])

  /** @see AccessWatchpointProfile#tryGetOrCreateAccessWatchpointRequest(String, String, JDIArgument*) */
  def onAccessWatchpoint(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[AccessWatchpointEvent]] =
    accessWatchpointProfile.tryGetOrCreateAccessWatchpointRequest(
      className,
      fieldName,
      extraArguments: _*
    )

  /** @see AccessWatchpointProfile#getOrCreateAccessWatchpointRequest(String, String, JDIArgument*) */
  def onUnsafeAccessWatchpoint(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): IdentityPipeline[AccessWatchpointEvent] =
    accessWatchpointProfile.getOrCreateAccessWatchpointRequest(
      className,
      fieldName,
      extraArguments: _*
    )

  /** @see AccessWatchpointProfile#getOrCreateAccessWatchpointRequestWithData(String, String, JDIArgument*) */
  def onUnsafeAccessWatchpointWithData(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): IdentityPipeline[AccessWatchpointEventAndData] =
    accessWatchpointProfile.getOrCreateAccessWatchpointRequestWithData(
      className,
      fieldName,
      extraArguments: _*
    )

  /** @see AccessWatchpointProfile#tryGetOrCreateAccessWatchpointRequestWithData(String, String, JDIArgument*) */
  def onAccessWatchpointWithData(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[AccessWatchpointEventAndData]] =
    accessWatchpointProfile.tryGetOrCreateAccessWatchpointRequestWithData(
      className,
      fieldName,
      extraArguments: _*
    )
}
