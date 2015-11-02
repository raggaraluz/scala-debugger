package org.senkbeil.debugger.api.profiles.pure.vm

import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.traits.vm.VMDeathProfile

/**
 * Represents a pure profile for vm death events that adds no
 * extra logic on top of the standard JDI.
 */
trait PureVMDeathProfile extends VMDeathProfile {
  /**
   * Constructs a stream of vm death events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of vm death events and any retrieved data based on
   *         requests from extra arguments
   */
  override def onVMDeathWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[VMDeathEventAndData] = ???
}
