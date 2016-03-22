package org.scaladebugger.api.dsl.vm

import com.sun.jdi.event.VMStartEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.vm.VMStartProfile

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param vmStartProfile The profile to wrap
 */
class VMStartDSLWrapper private[dsl] (
  private val vmStartProfile: VMStartProfile
) {
  /** Represents a VMStart event and any associated data. */
  type VMStartEventAndData = (VMStartEvent, Seq[JDIEventDataResult])

  /** @see VMStartProfile#tryGetOrCreateVMStartRequest(JDIArgument*) */
  def onVMStart(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[VMStartEvent]] =
    vmStartProfile.tryGetOrCreateVMStartRequest(extraArguments: _*)

  /** @see VMStartProfile#getOrCreateVMStartRequest(JDIArgument*) */
  def onUnsafeVMStart(
    extraArguments: JDIArgument*
  ): IdentityPipeline[VMStartEvent] =
    vmStartProfile.getOrCreateVMStartRequest(extraArguments: _*)

  /** @see VMStartProfile#getOrCreateVMStartRequestWithData(JDIArgument*) */
  def onUnsafeVMStartWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[VMStartEventAndData] =
    vmStartProfile.getOrCreateVMStartRequestWithData(extraArguments: _*)

  /** @see VMStartProfile#tryGetOrCreateVMStartRequestWithData(JDIArgument*) */
  def onVMStartWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[VMStartEventAndData]] =
    vmStartProfile.tryGetOrCreateVMStartRequestWithData(extraArguments: _*)
}
