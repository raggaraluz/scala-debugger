package org.scaladebugger.api.profiles.swappable.methods
import acyclic.file

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.methods.MethodExitRequestInfo
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfileManagement
import org.scaladebugger.api.profiles.traits.methods.MethodExitProfile

import scala.util.Try

/**
 * Represents a swappable profile for method exit events that redirects the
 * invocation to another profile.
 */
trait SwappableMethodExitProfile extends MethodExitProfile {
  this: SwappableDebugProfileManagement =>

  override def tryGetOrCreateMethodExitRequestWithData(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MethodExitEventAndData]] = {
    withCurrentProfile.tryGetOrCreateMethodExitRequestWithData(
      className,
      methodName,
      extraArguments: _*
    )
  }

  override def isMethodExitRequestPending(
    className: String,
    methodName: String
  ): Boolean = {
    withCurrentProfile.isMethodExitRequestPending(className, methodName)
  }

  override def isMethodExitRequestWithArgsPending(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Boolean = {
    withCurrentProfile.isMethodExitRequestWithArgsPending(
      className,
      methodName,
      extraArguments: _*
    )
  }

  override def methodExitRequests: Seq[MethodExitRequestInfo] = {
    withCurrentProfile.methodExitRequests
  }
}
