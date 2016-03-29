package org.scaladebugger.api.profiles.swappable.watchpoints
import acyclic.file

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.watchpoints.AccessWatchpointRequestInfo
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfileManagement
import org.scaladebugger.api.profiles.traits.watchpoints.AccessWatchpointProfile

import scala.util.Try

/**
 * Represents a swappable profile for access watchpoint events that redirects
 * the invocation to another profile.
 */
trait SwappableAccessWatchpointProfile extends AccessWatchpointProfile {
  this: SwappableDebugProfileManagement =>

  override def tryGetOrCreateAccessWatchpointRequestWithData(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[AccessWatchpointEventAndData]] = {
    withCurrentProfile.tryGetOrCreateAccessWatchpointRequestWithData(
      className,
      fieldName,
      extraArguments: _*
    )
  }

  override def isAccessWatchpointRequestPending(
    className: String,
    fieldName: String
  ): Boolean = {
    withCurrentProfile.isAccessWatchpointRequestPending(className, fieldName)
  }

  override def isAccessWatchpointRequestWithArgsPending(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Boolean = {
    withCurrentProfile.isAccessWatchpointRequestWithArgsPending(
      className,
      fieldName,
      extraArguments: _*
    )
  }

  override def accessWatchpointRequests: Seq[AccessWatchpointRequestInfo] = {
    withCurrentProfile.accessWatchpointRequests
  }
}
